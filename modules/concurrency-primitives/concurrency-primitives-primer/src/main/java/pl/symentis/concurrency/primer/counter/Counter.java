package pl.symentis.concurrency.primer.counter;

public class Counter {

	private volatile long counter;

	public void inc() {
		++counter;
	}

	public long counter() {
		return counter;
	}
}
