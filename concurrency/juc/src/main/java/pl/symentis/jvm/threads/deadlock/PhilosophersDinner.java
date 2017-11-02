package pl.symentis.jvm.threads.deadlock;

public class PhilosophersDinner {

	public static void main(String[] args) throws InterruptedException {
		Thread[] philosophers = new Thread[5];
		Chopstick[] chopsticks = new Chopstick[5];

		for (int i = 0; i < 5; ++i)
			chopsticks[i] = new Chopstick(i);
		for (int i = 0; i < 5; ++i) {
			philosophers[i] = new Thread(new Philosopher(chopsticks[i],
					chopsticks[(i + 1) % 5]));
			philosophers[i].start();
		}
		for (int i = 0; i < 5; ++i)
			philosophers[i].join();
	}
}
