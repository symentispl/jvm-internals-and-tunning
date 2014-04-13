package pl.symentis.jvm.threads.deadlock;

import java.util.Random;

class Philosopher implements Runnable {

	private Chopstick left, right;

	private Random random;

	private int thinkCount;

	public Philosopher(Chopstick left, Chopstick right) {
		this.left = left;
		this.right = right;
		random = new Random();
	}

	public void run() {
		try {
			while (true) {
				++thinkCount;
				if (thinkCount % 10 == 0)
					System.out.println("Philosopher " + this + " has thought "
							+ thinkCount + " times");
				Thread.sleep(random.nextInt(1000));
				synchronized (left) {
					synchronized (right) {
						Thread.sleep(random.nextInt(1000));
					}
				}
			}
		} catch (InterruptedException e) {
		}
	}
}
