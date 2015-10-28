package tracking;

import java.util.ArrayList;

public class ReservationDataset extends Dataset{

	private final boolean isAdvanced;
	private final Algorithm algorithm;
	private final int numClients;
	private final double clientActivity;

	private final ArrayList<Record> records;

	public ReservationDataset(Algorithm algorithm, int numClients, double clientActivity) {
		super("reservation-data");
		this.algorithm = algorithm;
		this.numClients = numClients;
		this.clientActivity = clientActivity;
		this.isAdvanced = algorithm == Algorithm.Footprint;
		records = new ArrayList<Record>();
	}

	public boolean isAdvanced() {
		return isAdvanced;
	}

	public void add(int userID, long bytes, int attempt, double chance, String withdrawBehaviour) {
		records.add(new Record(algorithm, numClients, clientActivity, userID, bytes, attempt, chance, withdrawBehaviour));
	}

	public void add(int userID, long bytes, int attempt) {
		add(userID, bytes, attempt, 0d, "");
	}

	@Override
	public String toString(boolean includeHeader) {
		StringBuilder sb = new StringBuilder();
		if(includeHeader) {
			sb.append(getHeader() + '\n');
		}
		for(Record r: records) {
			sb.append(r.toString() + '\n');
		}
		return sb.toString();
	}

	private String getHeader() {
		return "Algorithm,Clients,Activity,userId,Bytes,Attempt,Chance,WithdrawBehaviour";
	}

	protected class Record {
		public final Algorithm algorithm;
		public final int numClients;
		public final double clientActivity;
		public final int userID;
		public final long bytes;
		public final int attempt;
		public final double chance;
		public final String withdrawBehaviour;

		public Record(Algorithm algorithm, int numClients, double clientActivity, int userID, long bytes, int attempt, double chance, String withdrawBehaviour) {
			this.algorithm = algorithm;
			this.numClients = numClients;
			this.clientActivity = clientActivity;
			this.userID = userID;
			this.bytes = bytes;
			this.attempt = attempt;
			this.chance = chance;
			this.withdrawBehaviour = withdrawBehaviour;
		}

		@Override
		public String toString() {
			return 
				algorithm.name() + "," +
				numClients + "," +
				clientActivity + "," +
				userID + "," +
				bytes + "," +
				attempt + "," +
				chance + "," +
				withdrawBehaviour;
		}
	}
}