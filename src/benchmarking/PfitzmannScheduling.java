package benchmarking;

import java.util.Random;
import java.util.Arrays;

import tracking.StatisticsTracker;
import tracking.RoundDataset;

public class PfitzmannScheduling {
	private Random r;
	// The number of clients
	private int c;
	// The number of slots
	private int s;
	// The percentage of active (i.e. sending) clients
	private double a;
	// The size of one message
	private int msgSize;
	// The slot that each client chose
	private long[] choices;
	private long[] schedule;

	// Statistical data
	private int requiredRounds = 0;

	private StatisticsTracker tracker;
	private RoundDataset roundData;


	public PfitzmannScheduling(	int numSamples, 
								int numClients, 
								int numSlots, 
								double activity, 
								StatisticsTracker tracker,
								RoundDataset roundData) {
		this.c = numClients;
		this.s = numSlots;
		this.a = activity;
		this.tracker = tracker;
		this.roundData = roundData;
		// Calculate size of each message
		// according to Pfitzmann's description
		msgSize = 1;
		long alphabet = (long) c * (long) s;
		while(alphabet > 0) {
			alphabet >>=1;
			msgSize++;
		}
		// Add one bit that stores the count
		msgSize += 1;
		// Round up to full bytes
		// msgSize += (8-(msgSize%8));
		
		r = new Random();
	}

	public void schedule() {
		requiredRounds = 0;
		//Assure that at least one client wants to send something
		int start = 0;
		while(start == 0) {
			choices = new long[c];
			schedule = new long[s];
			for(int i = 0; i < c; i++) {
				if(r.nextDouble() < a) {
					// Add 1 to avoid that stations schedule slot 0
					choices[i] = (long) r.nextInt(s) + 1;
					start = 1;
				} else {
					choices[i] = -1;
				}
			}
		}
		
		long[] outcome = send(s);
		deduce(outcome[0], outcome[1]);

		if(tracker.reportRound()) {
			int emptySlots = 0;
			int collisions = 0;
			for(int i = 0; i < s; i++) {
				if(schedule[i]==0) {
					emptySlots++;
				} else if(schedule[i] > 1) {
					collisions++;
				}
			}
			int successfulReservations = s - collisions - emptySlots;
			double data = (double) (requiredRounds * msgSize) / (double) successfulReservations;
			roundData.add(collisions, requiredRounds, emptySlots, data);
		}
		
	}

	private void deduce(long sum, long count) {
		if(count == 1) {
			schedule[(int) sum-1]++;
			return;
		} else {
			long avg = sum / count;
			long[] outcome = send(avg);
			long newSum = outcome[0];
			long newCount = outcome[1];
			if(newSum == sum && newCount == count) {
				assert(sum % count == 0);
				long slot = sum/count;
				schedule[(int) slot-1] += count;
			} else {
				deduce(newSum, newCount);
				sum -= newSum;
				count -= newCount;
				deduce(sum, count);
			}
		}
	}

	private long[] send(long avgThreshold) {
		requiredRounds++;
		long sum = 0;
		long count = 0;
		for(int i = 0; i < c; i++) {
			if(choices[i] == -1) continue;
			if(schedule[(int) choices[i] - 1 ] == 0 && choices[i] <= avgThreshold) {
				sum += choices[i];
				count++;
			}
		}
		return new long[]{sum, count};
	}
}