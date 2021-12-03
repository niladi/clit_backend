package clit.eval.explainer;

public class DistanceEntry<T> implements Comparable<DistanceEntry<T>> {
	private T entry;
	private final Integer distance;

	public DistanceEntry(final Integer distance, T entry) {
		this.entry = entry;
		this.distance = distance;
	}

	public T getEntry() {
		return this.entry;
	}

	public Integer getDistance() {
		return this.distance;
	}

	@Override
	public int compareTo(DistanceEntry<T> o) {
		return this.distance.compareTo(o.distance);
	}

	@Override
	public String toString() {
		return entry + ": " + distance;
	}
}
