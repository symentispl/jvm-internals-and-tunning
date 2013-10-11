package pl.symentis.jvminternals;

public class Counter {

	private long counter;

	public void inc() {
		++counter;
	}

	public long counter() {
		return counter;
	}
}
