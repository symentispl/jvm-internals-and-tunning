package pl.symentis.concurrency.primer.counter;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicCounter {

	private AtomicLong counter = new AtomicLong();

	public long inc() {
		return counter.incrementAndGet();
	}

	public long counter() {
		return counter.longValue();
	}
}
