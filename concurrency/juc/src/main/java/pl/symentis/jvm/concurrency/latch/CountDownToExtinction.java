package pl.symentis.jvm.concurrency.latch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CountDownToExtinction {

	private static final class RandomNumbers implements Runnable {
		private final CountDownLatch countDownLatch;
		private final Random random = new Random();
		private List<Integer> numbers;

		private RandomNumbers(CountDownLatch countDownLatch) {
			this.countDownLatch = countDownLatch;
		}

		@Override
		public void run() {
			numbers = new ArrayList<>();

			for (int i = 0; i < 1000; i++) {
				numbers.add(random.nextInt(100));
			}

			countDownLatch.countDown();
		}

	}

	public static void main(String[] args) throws InterruptedException {

		int nrOfTasks = 100;

		final CountDownLatch countDownLatch = new CountDownLatch(nrOfTasks);

		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);

		List<RandomNumbers> allRandomNumbers = new ArrayList<>();

		for (int i = 0; i < 100; i++) {
			RandomNumbers randomNumbers = new RandomNumbers(countDownLatch);
			allRandomNumbers.add(randomNumbers);
			fixedThreadPool.execute(randomNumbers);
		}

		countDownLatch.await();

		System.out.println("policzone");
	}

}
