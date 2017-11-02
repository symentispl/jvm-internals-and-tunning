package pl.symentis.jvminternals.gc;

import java.util.Random;
import java.util.WeakHashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

/**
 * @author jaroslaw.palka@symentis.pl
 * 
 */
public class SecondGarbageCollectorCase {

	public static class B {

		@SuppressWarnings("unused")
		private B b;

		@SuppressWarnings("unused")
		private String value;

		public B(B b, String value) {
			super();
			this.b = b;
			this.value = value;
		}

	}

	public static void main(String[] args) {

		WeakHashMap<B, String> mapB = new WeakHashMap<B, String>();

		Stopwatch stopwatch = SimonManager.getStopwatch("gc-bench");
		Random random = new Random();
		while (true) {
			Split split = stopwatch.start();
			int i = 0;
			B prevB = null;
			while (i < 100000) {

				String keyB = RandomStringUtils.randomAlphabetic(2);
				String valueB = RandomStringUtils.random(random.nextInt(1024) + 1);

				B b = new B(prevB, keyB);
				mapB.put(b, valueB);

				prevB = b;
				i++;

			}
			split.stop();
			System.out.println(stopwatch);
		}
	}
}
