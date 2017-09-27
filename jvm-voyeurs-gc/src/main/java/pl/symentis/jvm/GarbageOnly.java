package pl.symentis.jvm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GarbageOnly {

	private final static Object[] memory = new Object[8192];

	// object size in bytes
	private static final int DEFAULT_NUMBEROFTHREADS = 64;
	private static final int DEFAULT_OBJECTSIZE = 32000;

	private static int numberOfThreads = DEFAULT_NUMBEROFTHREADS;
	private static int objectSize = DEFAULT_OBJECTSIZE;

	private static boolean running = true;

	public static void main(String[] args) throws Exception {

		// first (optional) argument is the number of threads to run
		if (args.length > 0) {
			numberOfThreads = Integer.parseInt(args[0]);
			// second (otpional) argument is the size of the objects
			if (args.length > 1) {
				objectSize = Integer.parseInt(args[1]);
			}
		}
		System.out.println("Creating objects of size " + objectSize + " with " + numberOfThreads + " threads");

		ExecutorService executorService = Executors.newCachedThreadPool();

		// run the configured number of GC producer threads
		for (int i = 0; i < numberOfThreads; i++) {
			executorService.submit(new GCProducer());
		}

		Thread.sleep(300000);
		running = false;
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);
	}

	public static class GCProducer implements Runnable {

		@Override
		public void run() {
			int osize = objectSize;
			int i = 0;
			while (true) {
				memory[i++] = new byte[osize];
				if (i >= memory.length) {
					i = 0;
				}
				if (i % 100 == 0) {
					synchronized (memory) {
						if (!running) {
							break;
						}
					}
				}
			}
		}
	}
}
