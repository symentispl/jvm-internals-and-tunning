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
public class FirstGarbageCollectorCase {

	public static class A {
		@SuppressWarnings("unused")
		private String value;

		public A(String value) {
			super();
			this.value = value;
		}

	}

	public static class B {

		@SuppressWarnings("unused")
		private A a;
		@SuppressWarnings("unused")
		private String value;

		public B(A a, String value) {
			super();
			this.a = a;
			this.value = value;
		}

	}

	public static void main(String[] args) {

		WeakHashMap<A, String> mapA = new WeakHashMap<A, String>();
		WeakHashMap<B, String> mapB = new WeakHashMap<B, String>();

		Stopwatch stopwatch = SimonManager.getStopwatch("gc-bench");
		Random random = new Random();
		while (true) {
			Split split = stopwatch.start();
			int i = 0;
			while (i < 100000) {
				String keyA = RandomStringUtils
						.random(random.nextInt(1024) + 1);
				String valueA = RandomStringUtils
						.random(random.nextInt(1024) + 1);

				A a = new A(keyA);
				mapA.put(a, valueA);

				String keyB = RandomStringUtils
						.random(random.nextInt(1024) + 1);
				String valueB = RandomStringUtils
						.random(random.nextInt(1024) + 1);

				B b = new B(a, keyB);
				mapB.put(b, valueB);

				i++;

			}
			split.stop();
			System.out.println(stopwatch);
		}
	}
}
