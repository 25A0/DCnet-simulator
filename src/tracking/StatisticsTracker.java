package tracking;

import java.util.Arrays;

/**
 *	This class is used to keep track of the measurements
 * 	of a scheduling simulation run. 
 */
public class StatisticsTracker {

	// The number of samples that will be recorded per client
	private final int samples;
	// The number of samples that have so far been recorded, per client
	private int[] observations;
	// The number of samples that have so far been recorded, overall
	private int overallObservations;
	// The total number of expected observations
	private int LIMIT_OBSERVATIONS;
	// The number of clients that are being simulated
	private int clients;

	// A boolean that indicates whether gathering information has finished,
	// and the results are ready to be read.
	private boolean isFinished;
	
	/**
	 * Instantiates a new tracker for a simulation run.
	 * @param  samples The number of samples that will be recorded *for each client*. Any additional samples will not be taken into account.
	 * @param  clients The number of clients that are being simulated.
	 * @return
	 */	
	public StatisticsTracker(int samples, int clients) {
		this.samples = samples;
		this.clients = clients;
		this.observations = new int[clients];
		this.overallObservations = 0;
		this.LIMIT_OBSERVATIONS = samples * clients;
		this.isFinished = false;
	}

	public StatisticsTracker(int samples) {
		this.samples = samples;
		this.overallObservations = 0;
		this.LIMIT_OBSERVATIONS = samples;
		this.isFinished = false;
	}

	public double getProgress() {
		return (double) overallObservations / (double) LIMIT_OBSERVATIONS;
	}

	public boolean isFinished() {
		return this.isFinished;
	}

	public boolean reportRound() {
		if(overallObservations >= LIMIT_OBSERVATIONS) {
			return false;
		} else {
			overallObservations++;
			if(overallObservations == LIMIT_OBSERVATIONS) {
				finish();
			}
			return true;
		}
	}

	/**
	 * Reports a successful reservation.
	 * @param userID	An integer in the range [0, clients) that identifies the user across multiple calls of this function.
	 * @return  True iff this reservation is still relevant for the measurement
	 */
	public boolean reportReservation(int userID) {
		if(observations[userID] >= samples) {
			return false;
		}
		int index = observations[userID] * clients + userID;
		if(index >= LIMIT_OBSERVATIONS) {
			throw new IndexOutOfBoundsException("UserID: " + userID + "\nObservations[userID]: " + observations[userID]);
		}
		observations[userID]++;
		overallObservations++;
		if(overallObservations == LIMIT_OBSERVATIONS) {
			finish();
		}
		return true;
	}

	private void finish() {
		isFinished = true;
	}

}