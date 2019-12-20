package pl.symentis.concurrency.mapreduce;

import java.util.Iterator;

final class IteratorInput<E> implements Input<E> {

	private final Iterator<E> iterator;

	IteratorInput(Iterator<E> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public E next() {
		return iterator.next();
	}

}