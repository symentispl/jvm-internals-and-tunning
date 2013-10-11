package pl.symentis.jvminternals;

import java.util.concurrent.locks.ReentrantLock;

public class LockedCounter {

	private long counter;

	private final ReentrantLock lock = new ReentrantLock();

	public void inc() {
		lock.lock();
		try {
			++counter;

		} finally {
			lock.unlock();
		}
	}

	public long counter() {
		return counter;
	}
}
