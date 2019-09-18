package pl.symentis.concurrency.primer.counter;

public class SynchronizedCounter {

	private long counter;

	public synchronized long inc() {
		return ++counter;
	}

	public synchronized long counter() {
		return counter;
	}
}
