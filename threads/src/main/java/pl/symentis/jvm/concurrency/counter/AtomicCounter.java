package pl.symentis.jvm.concurrency.counter;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicCounter {

	private AtomicLong counter = new AtomicLong();

	public void inc() {
		counter.incrementAndGet();
	}

	public long counter() {
		return counter.longValue();
	}
}
