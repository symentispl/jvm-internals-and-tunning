package pl.symentis.jvm.concurrency.lockfree;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class LinkedList<V> implements Iterable<V> {

	private static class Cell<V> {
		V value;
		AtomicReference<Cell<V>> nextRef = new AtomicReference<>();
	}

	private final Cell<V> head;
	private final Cell<V> tail;

	public LinkedList() {

		head = new Cell<V>();

		tail = new Cell<V>();
		head.nextRef.set(tail);
	}

	public void add(V value) {

		Cell<V> cell = new Cell<>();
		cell.value = value;
		cell.nextRef.set(tail);

		Cell<V> current = head;
		while (!current.nextRef.compareAndSet(tail, cell)) {
			current = current.nextRef.get();
			if (current == null) {
				break;
			}
		}

	}

	@Override
	public Iterator<V> iterator() {
		return new Iterator<V>() {

			private Cell<V> current = LinkedList.this.head.nextRef.get();

			@Override
			public boolean hasNext() {
				return current.nextRef.get() != null;
			}

			@Override
			public V next() {
				V valueRef = current.value;
				current = current.nextRef.get();
				return valueRef;
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub

			}
		};
	}

	public static void main(String[] args) {
		LinkedList<String> list = new LinkedList<String>();

		list.add("dupa");
		list.add("cipa");
		list.add("chuj");

		for (String s : list) {
			System.out.println(s);
		}

	}

}
