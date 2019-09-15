package pl.symentis.concurrency.primer.barrier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 *
 */
public class AverageRandomNumbers {

	private static class RandomNumbers implements Runnable {

		private final Random random = new Random();
		private final CyclicBarrier barrier;
		private boolean running = true;
		private int nextInt;

		public RandomNumbers(CyclicBarrier barrier) {
			this.barrier = barrier;
		}

		@Override
		public void run() {
			while (running) {
				nextInt = random.nextInt(100);
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
			}
		}

		public int nextInt() {
			return nextInt;
		}

	}

	public static void main(String[] args) {

		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
		final List<RandomNumbers> allRandomNumbers = new ArrayList<>();

		CyclicBarrier barrier = new CyclicBarrier(10, new Runnable() {

			@Override
			public void run() {
				int sum = 0;
				for (RandomNumbers numbers : allRandomNumbers) {
					sum += numbers.nextInt();
				}
				System.out.println(sum / allRandomNumbers.size());
			}
		});

		for (int i = 0; i < 10; i++) {

			RandomNumbers randomNumbers = new RandomNumbers(barrier);
			allRandomNumbers.add(randomNumbers);
			fixedThreadPool.execute(randomNumbers);
		}
		

	}
}
