package pl.symentis.jvm.concurrency.semaphore;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

public class ObjectPool {

	private Semaphore semaphore;
	private CopyOnWriteArrayList<Object> unused;

	public ObjectPool() {
		semaphore = new Semaphore(10);

		unused = new CopyOnWriteArrayList<>();

	}

	public Object borrow() throws InterruptedException {
		semaphore.acquire();

		unused.iterator();
		
		return null;

	}

	public static void main(String[] args) {

	}

}
