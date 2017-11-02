package pl.symentis.jvm.concurrency.counter;

import static java.lang.System.out;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RefExample {

	private static class A {

		private int batch;
		private int level;
		private A a;

		public A(A a) {
			this.a = a;
		}
	}

	public static void main(String[] args) {

		final ReferenceQueue<Object> refQ = new ReferenceQueue<>();
		final List<WeakReference<A>> refs = Collections
				.synchronizedList(new ArrayList<WeakReference<A>>());

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					while (true) {
						Reference<? extends Object> ref = refQ.remove();
						out.println("ref " + ref + " became weakly reachable");
						refs.remove(ref);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}).start();

		while (true) {
			A root = null;
			A prev = null;
			for (int i = 0; i < 10000; i++) {
				A a = new A(prev);
				if (root == null) {
					root = a;
				}

				refs.add(new WeakReference<RefExample.A>(a, refQ));

				prev = a;
			}
		}

	}
}