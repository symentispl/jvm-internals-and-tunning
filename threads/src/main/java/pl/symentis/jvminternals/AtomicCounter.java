package pl.symentis.jvminternals;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicCounter {

	private AtomicLong counter;

	public void inc() {
		counter.incrementAndGet();
	}

	public long counter() {
		return counter.longValue();
	}
}
