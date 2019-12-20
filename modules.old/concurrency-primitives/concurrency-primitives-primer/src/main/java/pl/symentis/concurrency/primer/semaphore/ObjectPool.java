package pl.symentis.concurrency.primer.semaphore;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ObjectPool {

	private Semaphore semaphore;
	private AtomicReferenceArray<Boolean> a = new AtomicReferenceArray<>(10);

	public ObjectPool() {
		semaphore = new Semaphore(10);

	}

	public Object borrow() throws InterruptedException {
		semaphore.acquire();
		
		return null;
	}

	public static void main(String[] args) {

	}

}
