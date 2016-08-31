package pl.symentis.jvm.concurrency.counter;

public class Counter {

	private long counter;

	public void inc() {
		++counter;
	}

	public long counter() {
		return counter;
	}
}
