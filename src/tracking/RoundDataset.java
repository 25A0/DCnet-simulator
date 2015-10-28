package tracking;

import java.util.ArrayList;

public class RoundDataset extends Dataset{

	private final ArrayList<Record> records;

	private final Algorithm algorithm;
	private final int numSlots, numRounds, numBits, numClients;
	private final double clientActivity;

	public RoundDataset(Algorithm algorithm, int numSlots, int numRounds, int numBits, int numClients, double clientActivity) {
		super("round-data");
		this.algorithm = algorithm;
		this.numSlots = numSlots;
		this.numRounds = numRounds;
		this.numBits = numBits;
		this.numClients = numClients;
		this.clientActivity = clientActivity;
		records = new ArrayList<Record>();
	}

	public void add(int collisions, int requiredRounds, int emptySlots, double data) {
		records.add(new Record(collisions, requiredRounds, emptySlots, data));
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
		return "Algorithm,Slots,Rounds,Bits,Clients,Activity,Collisions,ReqRounds,EmptySlots,Data";
	}

	protected class Record {
		public final int collisions, requiredRounds, emptySlots;
		public final double data;

		public Record(int collisions, int requiredRounds, int emptySlots, double data) {
			this.collisions = collisions;
			this.requiredRounds = requiredRounds;
			this.emptySlots = emptySlots;
			this.data = data;
		}

		@Override
		public String toString() {
			return
				algorithm.toString() + ","+
				numSlots +","+
				numRounds +","+
				numBits +","+
				numClients +","+
				clientActivity +","+
				collisions +","+
				requiredRounds +","+
				emptySlots + "," +
				data;
		}
	}
}