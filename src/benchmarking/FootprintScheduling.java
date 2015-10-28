package benchmarking;

import java.util.Random;
import java.util.Arrays;

import tracking.StatisticsTracker;
import tracking.ReservationDataset;
import tracking.RoundDataset;

public class FootprintScheduling {
	private Random r;
	private int c;
	private int s;
	private int b;
	private int rounds;
	private double a;
	private boolean stopOnConvergence;
	
	// The withdraw behaviour to be used
	private final WithdrawBehaviour withdrawBehaviour;
	private final double percentage;

	private StatisticsTracker tracker;
	private RoundDataset roundData;

	// Sentinel values for choices
	private int INACTIVE = -2, WITHDRAWN = -1;

	public enum WithdrawBehaviour {
		Static, Linear, Reactive;

		@Override
		public String toString() {
			return name();
		}
	}

	public FootprintScheduling(int numSamples, int numClients, int numSlots, int numRounds, int numBits, double activity, 
			boolean stopOnConvergence, WithdrawBehaviour withdrawBehaviour, double percentage, StatisticsTracker tracker, 
			RoundDataset roundData) {
		this.roundData = roundData;
		this.c = numClients;
		this.s = numSlots;
		this.b = numBits;
		this.rounds = numRounds;
		this.a = activity;
		this.withdrawBehaviour = withdrawBehaviour;
		this.percentage = percentage;
		this.tracker = tracker;
		this.stopOnConvergence = stopOnConvergence;
		
		r = new Random();
	}

	public void scheduleSingle() {
		if(b > 8) {
			System.out.println("More than 8 bits per slot are not supported");
			return;
		}
		boolean succeeded = false;
		int requiredRounds = rounds;

		// A convenient schedule array
		byte[] schedule = new byte[s];

		// An array for the clients to store their footprints in
		byte[] footprints = new byte[c];

		// Keeps track of reservations per client
		int[] choices = new int[c];
		// int countA = 0, countI = 0;
		int numActive = (int) (a * (double) c);
		for(int i = 0; i < c; i++) {
			// A client participates if there is a pending package that 
			// has not yet been sent, or if this client is randomly chosen
			// based on the activity rate.
			if(i < numActive) {
				choices[i] = r.nextInt(s);
				footprints[i] = getFootprint(b);
				schedule[choices[i]] ^= footprints[i];
			} else {
				choices[i] = INACTIVE;
			}
		}
		// System.out.println("Activity rate: " + a);
		// System.out.println("Active/Inactive: " + countA + '/' + countI);
		
		for (int round = 0; round < rounds; round++) {	
			// System.out.println("Schedule is: " + Arrays.toString(schedule));
			// System.out.println("Choices are: " + Arrays.toString(choices));
			byte[] nextSchedule = new byte[s];
			for (int cl = 0; cl < c; cl++) {
				if(choices[cl] == INACTIVE) continue;
				int choice = chooseSingle(schedule, choices[cl], footprints[cl], round == rounds - 1);
				footprints[cl] = getFootprint(b);
				if(choice != WITHDRAWN) {
					footprints[cl] = getFootprint(b);
					nextSchedule[choice] ^= footprints[cl];
				}
				choices[cl] = choice;
			}
			schedule = nextSchedule;
			int collisions = numCollisionsSingle(choices);
			if(collisions == 0) {
				if(!succeeded) {
					// update # required rounds:
					requiredRounds = round + 1;
				}
				succeeded = true;
				if (stopOnConvergence) break;
			} else {
				requiredRounds = rounds;
				succeeded = false;
			}
		}

		if(tracker.reportRound()) {
			int emptySlots = 0;
			int collisions = 0;
			int[] slots = new int[s];
			for(int i = 0; i < c; i++) {
				if(choices[i] == INACTIVE || choices[i] == WITHDRAWN) continue;
				slots[choices[i]]++;
			}
			for(int i = 0; i < s; i++) {
				if(slots[i] == 0) {
					emptySlots++;
				} else if(slots[i] > 1) {
					collisions++;
				}
			}	
			// The amount of data that each client sent in this scheduling cycle
			int successfulReservations = s - emptySlots - collisions;
			double data = (double) (b*s*rounds) / (double) successfulReservations;
			roundData.add(collisions, requiredRounds, emptySlots, data);
		}		
	}

	private int chooseSingle(byte[] schedule, int lastChoice, byte footprint, boolean lastRound) {
		// A client that is already in withdrawn state will not
		// attempt to re-enter the scheduling
		if(lastChoice == WITHDRAWN) {
			return WITHDRAWN;
		} 

		// From here on we can assume that slot i was a slot
		// that we were trying to reserve
		if(schedule[lastChoice] == footprint) {
			// If there's no collision, we will keep reserving this slot.
			return lastChoice;
		} else if(lastRound) {
			// There was a collision and there's no time left to try more things
			return WITHDRAWN;
		} else {
			// There was a collision
			if(withdraw()) {
				return WITHDRAWN;
			} else {
				if(r.nextDouble() < 0.5) {
					return lastChoice;
				} else {
					// Determine the free slots
					int numFree = 0;
					int[] freeSlots = new int[s];
					for(int j =0; j < s; j++) {
						// Scan current schedule for free slots
						// and prevent that we 'move' to a slot
						// that we're currently trying to reserve.
						if(schedule[j] == 0 && lastChoice != j) {
							// Keep that slot in mind
							freeSlots[numFree] = j;
							numFree++;
						}
					}
					
					if(numFree == 0) {
						return WITHDRAWN;
					} else {
						// We will now try to reserve a random free slot instead of slot i.
						return freeSlots[r.nextInt(numFree)];
					}
				}
			}
		}
	}

	public void scheduleMultiple() {
		if(b > 8) {
			System.out.println("More than 8 bits per slot are not supported");
			return;
		}
		boolean succeeded = false;
		int requiredRounds = rounds;

		// A convenient schedule array
		byte[] schedule = new byte[s];

		// An array for the clients to store their footprints in
		byte[][] footprints = new byte[c][];

		// Keeps track of reservations per client
		boolean[][] choices = new boolean[c][];
		// int countA = 0, countI = 0;
		int numActive = (int) (a * (double) c);
		for(int i = 0; i < c; i++) {
			// A client participates if there is a pending package that 
			// has not yet been sent, or if this client is randomly chosen
			// based on the activity rate.
			if(i < numActive) {
				choices[i] = new boolean[s];
				footprints[i] = new byte[s];
				for (int j = 0; j < s; j++) {
					choices[i][j] = true;
					footprints[i][j] = getFootprint(b);
					schedule[j] ^= footprints[i][j];
				}
			} else {
				choices[i] = null;
			}
		}
		// System.out.println("Activity rate: " + a);
		// System.out.println("Active/Inactive: " + countA + '/' + countI);
		
		for (int round = 0; round < rounds; round++) {	
			// System.out.println("Schedule is: " + Arrays.toString(schedule));
			// System.out.println("Choices are: " + Arrays.toString(choices));
			byte[] nextSchedule = new byte[s];
			for (int cl = 0; cl < c; cl++) {
				if(choices[cl] == null) continue;
				boolean[] choice = chooseMultiple(schedule, choices[cl], footprints[cl], round == rounds - 1);
				for (int i = 0; i < s; i++) {
					footprints[cl][i] = getFootprint(b);
					if(choice[i]) {
						nextSchedule[i] ^= footprints[cl][i];
					}
				}
				choices[cl] = choice;
			}
			schedule = nextSchedule;
			int collisions = numCollisionsMultiple(choices);
			if(collisions == 0) {
				if(!succeeded) {
					// update # required rounds:
					requiredRounds = round + 1;
				}
				succeeded = true;
				if (stopOnConvergence) break;
			} else {
				requiredRounds = rounds;
				succeeded = false;
			}
		}

		if(tracker.reportRound()) {
			int emptySlots = 0;
			int collisions = 0;
			int[] slots = new int[s];
			for(int i = 0; i < c; i++) {
				if(choices[i] == null) continue;
				for (int j = 0; j < s; j++) {
					if(choices[i][j]) {
						slots[j]++;
					}
				}
			}
			for(int i = 0; i < s; i++) {
				if(slots[i] == 0) {
					emptySlots++;
				} else if(slots[i] > 1) {
					collisions++;
				}
			}	
			// The amount of data that each client sent in this scheduling cycle
			int successfulReservations = s - emptySlots - collisions;
			double data = (double) (b*s*rounds) / (double) successfulReservations;
			roundData.add(collisions, requiredRounds, emptySlots, data);
		}		
	}

	private boolean[] chooseMultiple(byte[] schedule, boolean[] lastChoice, byte[] footprint, boolean lastRound) {
		// A client that is already in withdrawn state will not
		// attempt to re-enter the scheduling
		if(lastChoice == null) {
			return null;
		} 
		// The new choices that we'll return in the end.
		// The boolean array will initially contain nothing but
		// 'false' values.
		boolean[] newChoice = new boolean[s];
		
		// Check how to proceed for each slot separately
		for (int i = 0; i < s; i++) {
			// We can just skip those slots that we're not
			// trying to reserve.
			if(!lastChoice[i]) continue;

			// From here on we can assume that slot i was a slot
			// that we were trying to reserve
			if(schedule[i] == footprint[i]) {
				// If there's no collision, we will keep reserving this slot.
				newChoice[i] = true;
			} else if(lastRound) {
				// There was a collision and there's no time left to try more things
				newChoice[i] = false;
			} else {
				// There was a collision
				if(withdraw()) {
					newChoice[i] = false;
				} else {
					if(r.nextDouble() < 0.5) {
						newChoice[i] = true;
					} else {
						// Determine the free slots
						int numFree = 0;
						int[] freeSlots = new int[s];
						for(int j =0; j < s; j++) {
							// Scan current schedule for free slots
							// and prevent that we 'move' to a slot
							// that we're currently trying to reserve.
							if(schedule[j] == 0 && !lastChoice[j] && !newChoice[j]) {
								// Keep that slot in mind
								freeSlots[numFree] = j;
								numFree++;
							}
						}
						
						if(numFree == 0) {
							newChoice[i] = false;
						} else {
							// We will now try to reserve a random free slot instead of slot i.
							newChoice[freeSlots[r.nextInt(numFree)]] = true;
							// For the record, newChoice[i] will be 'false' automatically.
						}
					}
				}
			}
		}
		return newChoice;
	}

	private boolean withdraw() {
		double chance = getChance();
		if(chance < 0) {
			chance = 0;
		} else if(chance > 1) {
			chance = 1;
		}
		return r.nextDouble() <= chance;
	}

	private double getChance() {
		return percentage;
	}

	private byte getFootprint(int b) {
		byte f = (byte) (r.nextInt((1 << b) - 1) + 1);
		return f;
	}

	private int numCollisionsSingle(int[] choices) {
		int collisions = 0;
		int[] slots = new int[s];
		Arrays.fill(slots, WITHDRAWN);
		for(int i = 0; i < c; i++) {
			int choice = choices[i];
			if(choice == WITHDRAWN || choice == INACTIVE) continue;
			
			if(slots[choice] != WITHDRAWN) {
				// client i tried to reserve slot j, 
				// but it was already occupied by another client.
				collisions++;
			} else {
				// markt hat client i was reserving this slot.
				// The exact identity does not matter, it just needs
				// to be indistinguishable from an un-occupied slot.
				slots[choice] = i;
			}
		}
		return collisions;
	}

	private int numCollisionsMultiple(boolean[][] choices) {
		int collisions = 0;
		int[] slots = new int[s];
		Arrays.fill(slots, WITHDRAWN);
		for(int i = 0; i < c; i++) {
			boolean[] choice = choices[i];
			if(choice == null) continue;
			for (int j = 0; j < s; j++) {
				if(choice[j]) {
					if(slots[j] != WITHDRAWN) {
						// client i tried to reserve slot j, 
						// but it was already occupied by another client.
						collisions++;
					} else {
						// markt hat client i was reserving this slot.
						// The exact identity does not matter, it just needs
						// to be indistinguishable from an un-occupied slot.
						slots[j] = i;
					}
				}
			}
		}
		return collisions;
	}
}