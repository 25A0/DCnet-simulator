package tracking;

public abstract class Dataset {
	public final String namePrefix;

	public enum Algorithm {
		Footprint,
		Chaum,
		Pfitzmann
	}

	public Dataset(String namePrefix) {
		this.namePrefix = namePrefix;
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public abstract String toString(boolean includeHeader);
}