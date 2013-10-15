package pl.symentis.jvminternals.threads;

/**
 * Hello world!
 * 
 */
public class CounterRunner {

	public static void main(String[] args) throws InterruptedException {
		final AtomicCounter counter = new AtomicCounter();

		Runnable counterTask = new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < 10000; i++) {
					counter.inc();
				}
			}
		};

		Thread t1 = new Thread(counterTask);
		Thread t2 = new Thread(counterTask);
		Thread t3 = new Thread(counterTask);
		Thread t4 = new Thread(counterTask);

		t1.start();
		t2.start();
		t3.start();
		t4.start();

		t1.join();
		t2.join();
		t3.join();
		t4.join();

		System.out.println("Wartość licznika:" + counter.counter());

	}
}
