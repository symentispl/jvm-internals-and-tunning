package pl.symentis.concurrency.primer.counter;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockedCounter {

	private long counter;

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public long inc() {
		lock.writeLock().lock();
		try {
			return ++counter;

		} finally {
			lock.writeLock().unlock();
		}
	}

	public long counter() {
		lock.readLock().lock();
		try {
			return counter;
		} finally {
			lock.readLock().unlock();
		}
	}
}
