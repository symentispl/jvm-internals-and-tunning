package pl.symentis.concurrency.primer.counter;

import java.util.concurrent.locks.ReentrantLock;

public class LockedCounter {

	private long counter;

	private final ReentrantLock lock = new ReentrantLock();

	public long inc() {
		lock.lock();
		try {
			return ++counter;

		} finally {
			lock.unlock();
		}
	}

	public long counter() {
		lock.lock();
		try {
			return counter;
		} finally {
			lock.unlock();
		}
	}
}
