package pl.symentis.jvminternals.gc;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

/**
 * @author jaroslaw.palka@symentis.pl
 * 
 */
public class ThirdGarbageCollectorCase {

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

		HashMap<String, SoftReference<B>> list = new HashMap<String, SoftReference<B>>();
		Stopwatch stopwatch = SimonManager.getStopwatch("gc-bench");
		while (true) {
			Split split = stopwatch.start();
			int i = 0;
			B prevB = null;
			while (i < 100000) {

				String keyB = RandomStringUtils.randomAlphanumeric(2);
				String valueB = RandomStringUtils.random(2048);

				B b = new B(prevB, valueB);
				list.put(keyB,
						new SoftReference<ThirdGarbageCollectorCase.B>(b));

				prevB = b;
				i++;

			}
			split.stop();
			System.out.println(stopwatch);
		}
	}
}
