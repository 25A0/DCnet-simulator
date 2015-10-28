package benchmarking;

import java.util.Random;
import java.util.Arrays;

import tracking.StatisticsTracker;
import tracking.RoundDataset;

public class ChaumScheduling {
	private Random r;
	private int c;
	private int s;
	private double a;
	
	// Statistical data
	private long[] sentBits;
	private int[] attempts;

	private StatisticsTracker tracker;
	private RoundDataset roundD;

	public ChaumScheduling(int numSamples, int numSlots, int numClients, double activity, StatisticsTracker tracker, RoundDataset roundD) {
		this.c = numClients;
		this.a = activity;
		this.tracker = tracker;
		this.roundD = roundD;

		// The size of the scheduling vector follows the estimations
		// provided in the Chaum paper.
		this.s = numSlots;

		sentBits = new long[c];
		attempts = new int[c];

		r = new Random();
	}

	public void schedule() {
		
		// Clients choose the slot in which they want to send
		int[] choices = new int[c];
		int numActive = (int) (a * (double) c);
		for(int i = 0; i < c; i++) {
			if(i < numActive) {
				choices[i] = r.nextInt(s);
				sentBits[i] += s;
			} else {
				choices[i] = -1;
				continue;
			}
		}
		
		if(tracker.reportRound()) {
			int collisions = 0;
			int emptySlots = 0;
			int[] slots = new int[s];
			for(int i = 0; i < c; i++) {
				int choice = choices[i];
				if(choice != -1) {
					slots[choice]++;
				}
			}
			for (int i = 0; i < s; i++) {
				if(slots[i] == 0) {
					emptySlots++;
				} else if(slots[i] > 1) {
					collisions+= slots[i];
				}
			}
			int successfulReservations = s - emptySlots - collisions;
			double data = (double) s / (double) successfulReservations;
			roundD.add(collisions, 1, emptySlots, data);
		}
	}	
}