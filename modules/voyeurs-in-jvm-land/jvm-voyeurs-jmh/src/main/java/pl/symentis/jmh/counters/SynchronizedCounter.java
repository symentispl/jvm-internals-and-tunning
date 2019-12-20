package pl.symentis.jmh.counters;

public class SynchronizedCounter {

	private long counter;

	public synchronized void inc() {
		++counter;
	}

	public synchronized long counter() {
		return counter;
	}
}
