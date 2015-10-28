package cli;

import cli.CLC;
import cli.ArgSet;

import benchmarking.FootprintScheduling.WithdrawBehaviour;
import benchmarking.FootprintScheduling;
import benchmarking.ChaumScheduling;
import benchmarking.PfitzmannScheduling;

import tracking.RoundDataset;
import tracking.ReservationDataset;
import tracking.Dataset;
import tracking.StatisticsTracker;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Arrays;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SchedulingController extends CLC {
	private static final int NUM_SAMPLES = 100;

	public SchedulingController() {
		final ArrayList<Dataset> pendingSets = new ArrayList<Dataset>();

		Action footprintAction = new Action() {
			private int numSlots, numBits;
			private int numRounds = 128;
			private double activity;
			private boolean stopOnConvergence;
			private WithdrawBehaviour withdrawBehaviour;
			
			@Override
			public void execute(ArgSet args) {
				numSlots = args.fetchInteger();
				numBits = args.fetchInteger();
				Integer[] clients;
				Double[] percentage = null;
				try {
					ArgSet cas = args.fetchList();
					ArrayList<Integer> cl = new ArrayList<Integer>();
					while(cas.hasIntArg()) {
						cl.add(cas.fetchInteger());
					}
					clients = cl.toArray(new Integer[cl.size()]);
				} catch(InputMismatchException e) {
					clients = new Integer[] {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000};
				}
				if(args.hasStringArg()) {
					String wbString = args.fetchString();
					for(WithdrawBehaviour wb: WithdrawBehaviour.values()) {
						if(wbString.equalsIgnoreCase(wb.name())) {
							withdrawBehaviour = wb;
						}
					}

					try {
						ArgSet pas = args.fetchList();
						ArrayList<Double> pl = new ArrayList<Double>();
						while(pas.hasArg()) {
							pl.add(Double.valueOf(pas.pop()));
						}
						percentage = pl.toArray(new Double[pl.size()]);
					} catch(InputMismatchException e) {
						switch(withdrawBehaviour) {
							case Static: 
								percentage = new Double[]{0.7};
								break;
							case Linear: 
								percentage = new Double[]{0.002};
								break;
							case Reactive:
								percentage = new Double[]{0.2};
								break;
							default:
								throw new IllegalStateException("Chosen withdrawBehaviour was not covered by switch");
						}
					}
				}
				if(withdrawBehaviour == null) {
					withdrawBehaviour = WithdrawBehaviour.Static;
					percentage = new Double[]{0.75};
				}
				if(args.hasArg()) {
					activity = Double.valueOf(args.pop());
					if(args.hasArg()) {
						stopOnConvergence = Boolean.valueOf(args.pop());
					} else {
						stopOnConvergence = false;
					}
				} else {
					activity = 1.0;
					stopOnConvergence = false;
				}
				percentage = new Double[]{0.75};
				
				int cc = clients.length;
				int pp = percentage.length;
				System.out.println("Executing FOOTPRINT SCHEDULING benchmark for " +  numSlots + " slots, "+ numRounds+ " rounds and " + numBits + " bits per slot, " + activity*100 +"% client activity, using "+ withdrawBehaviour.name() + " Withdraw behaviour" + (stopOnConvergence?", stopping on convergence":""));
				for(int i = 0; i < cc; i++) {
					for(int j = 0; j < pp; j++) {
						numRounds = (int) (Math.log(clients[i]) / Math.log(2d));
						System.out.print("["+(i*pp+j+1)+"/"+(cc*pp)+"]\t" + clients[i] + " client(s)\t" + numRounds + " rounds\t" + numSlots + " slots\t" + withdrawBehaviour+" ("+percentage[j]+")\t");
						benchmark(NUM_SAMPLES, clients[i], percentage[j]);
						System.out.println("\t [DONE]");
					}
				}
				
				System.out.println(" DONE");
			}

			private void benchmark(int samples, int clients, double percentage) {
				StatisticsTracker tracker = new StatisticsTracker(samples);
				RoundDataset roundD = new RoundDataset(Dataset.Algorithm.Footprint, numSlots, numRounds, numBits, clients, activity);
				FootprintScheduling s = new FootprintScheduling(samples, clients, numSlots, numRounds, numBits, activity, stopOnConvergence, withdrawBehaviour, percentage, tracker, roundD);
				int progress = 0;
				int progressMilli = 0;
				do {
					s.scheduleMultiple();
					double currentProgress = tracker.getProgress();
					while((currentProgress * 100) > progress) {
						System.out.print(".");
						progress++;
					}
					if(currentProgress > 0.95d) {
						while(((currentProgress - 0.95d) * 1000) > progressMilli) {
							System.out.print("+");
							progressMilli++;
						}
					}
				} while(!tracker.isFinished());
				pendingSets.add(roundD);
			}
		};

		Action pfitzmannAction = new Action() {
			private int numSlots;
			private double activity;

			@Override
			public void execute(ArgSet args) {
				numSlots = args.fetchInteger();

				Integer[] clients;
				try {
					ArgSet cas = args.fetchList();
					ArrayList<Integer> cl = new ArrayList<Integer>();
					while(cas.hasIntArg()) {
						cl.add(cas.fetchInteger());
					}
					clients = cl.toArray(new Integer[cl.size()]);
				} catch(InputMismatchException e) {
					clients = new Integer[] {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000};
				}
				if(args.hasArg()){
					activity = Double.valueOf(args.pop());
				} else {
					activity = 1d;
				}
				int cc = clients.length;
				double[] bytesPerReservation = new double[cc];
				double[] byteSD = new double[cc];
				System.out.println("Executing PFITZMANN'S ALGORITHM benchmark for " + numSlots + " slots per client, " + activity*100 +"% client activity");
				for(int i = 0; i < cc; i++) {
					System.out.print("["+(i+1)+"/"+cc+"]\t" + clients[i] + " client(s)\t" + clients[i]*numSlots + " slots\t");
					benchmark(NUM_SAMPLES, clients[i]);
					System.out.println("\t [DONE]");
				}

				System.out.println(" DONE");
			}

			private void benchmark(int samples, int clients) {
				StatisticsTracker tracker = new StatisticsTracker(samples);
				RoundDataset roundD = new RoundDataset(Dataset.Algorithm.Pfitzmann, numSlots*clients, 1, numSlots, clients, activity);
				PfitzmannScheduling s = new PfitzmannScheduling(samples, clients, clients * numSlots, activity, tracker, roundD);
				int progress = 0;
				int progressMilli = 0;
				do {
					s.schedule();
					double currentProgress = tracker.getProgress();
					while((currentProgress * 100) > progress) {
						System.out.print(".");
						progress++;
					}
					if(currentProgress > 0.95d) {
						while(((currentProgress - 0.95d) * 1000) > progressMilli) {
							System.out.print("+");
							progressMilli++;
						}
					}
				} while(!tracker.isFinished());
				pendingSets.add(roundD);
			}
		};

		Action chaumAction = new Action() {
			private double activity;
			private int ratio;


			@Override
			public void execute(ArgSet args) {
				Integer[] clients;
				try {
					ArgSet cas = args.fetchList();
					ArrayList<Integer> cl = new ArrayList<Integer>();
					while(cas.hasIntArg()) {
						cl.add(cas.fetchInteger());
					}
					clients = cl.toArray(new Integer[cl.size()]);
				} catch(InputMismatchException e) {
					clients = new Integer[] {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000};
				}
				if(args.hasArg()) {
					activity = Double.valueOf(args.pop());
				} else {
					activity = 1d;
				}
				if(args.hasArg()) {
					ratio = Integer.valueOf(args.pop());
				} else {
					ratio = 32;
				}
				int cc = clients.length;
				double[] bytesPerReservation = new double[cc];
				double[] byteSD = new double[cc];
				System.out.println("Executing CHAUM benchmark for " + activity*100 +"% client activity");
				for(int i = 0; i < cc; i++) {
					System.out.print("["+(i+1)+"/"+cc+"]\t" + clients[i] + " client(s)\t");
					benchmark(NUM_SAMPLES, clients[i]);
					System.out.println("\t [DONE]");
				}
				System.out.println(" DONE");
			}

			private void benchmark(int samples, int clients) {
				StatisticsTracker tracker = new StatisticsTracker(samples);
				int numSlots = (int) ((double) ratio * (double) clients);
				RoundDataset roundD = new RoundDataset(Dataset.Algorithm.Chaum, numSlots, 1, ratio, clients, activity);
				ChaumScheduling s = new ChaumScheduling(samples, numSlots, clients, activity, tracker, roundD);
				int progress = 0;
				int progressMilli = 0;
				do {
					s.schedule();
					double currentProgress = tracker.getProgress();
					while((currentProgress * 100) > progress) {
						System.out.print(".");
						progress++;
					}
					if(currentProgress > 0.95d) {
						while(((currentProgress - 0.95d) * 1000) > progressMilli) {
							System.out.print("+");
							progressMilli++;
						}
					}
				} while(!tracker.isFinished());
				pendingSets.add(roundD);
			}
		};

		Action writeAction = new Action() {
			@Override
			public void execute(ArgSet args) {
				try {
					System.out.print("Writing " + pendingSets.size() + " sets to disk... ");
					for(Dataset d: pendingSets) {
						boolean includeHeader = false;
						String path = "benchmark-data/" + d.namePrefix + ".csv";
						File f = new File(path);
						if(!f.exists()) {
							includeHeader = true;
							f.getParentFile().mkdirs();
						}
						BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
						bw.write(d.toString(includeHeader));
						bw.close();
					}
					pendingSets.clear();
					System.out.println("DONE");
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		};

		Action clearAction = new Action() {
			@Override
			public void execute(ArgSet args) {
				pendingSets.clear();
			}
		};

		mapCommand("footprint", footprintAction);
		mapCommand("pfitzmann", pfitzmannAction);
		mapCommand("chaum", chaumAction);

		// pendingSet = new Dataset(false);
		mapCommand("write", writeAction);
		mapCommand("clear", clearAction);
	}

}