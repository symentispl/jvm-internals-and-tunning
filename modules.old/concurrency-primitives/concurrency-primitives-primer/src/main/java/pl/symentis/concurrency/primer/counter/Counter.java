package pl.symentis.concurrency.primer.counter;

public class Counter {

	private long counter;

	public long inc() {
		return ++counter;
	}

	public long counter() {
		return counter;
	}
}
