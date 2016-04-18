/**
 *   Copyright 2013 Symentis.pl
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package pl.symentis.jvm.concurrency.lockfree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;

/**
 * Just placeholder for highly optimizing structure for logging scenario events.
 * Something more like a redo log.
 * 
 * @author jaroslaw.palka@symentis.pl
 * 
 * @param <V>
 */
public class Redo<V> implements Iterable<V> {

	private static class RedoIterator<V> implements Iterator<V> {

		private Node<V> currentNode;

		private RedoIterator(Node<V> currentNode) {
			super();
			this.currentNode = currentNode;
		}

		@Override
		public boolean hasNext() {
			return currentNode != null;
		}

		@Override
		public V next() {

			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			Node<V> node = currentNode;

			currentNode = node.prev;

			return node.value;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private static class Node<V> {
		V value;

		Node<V> prev;
	}

	private final AtomicReference<Node<V>> tailRef = new AtomicReference<>();

	public void add(V item) {
		Node<V> node = new Node<V>();
		Node<V> tail = tailRef.getAndSet(node);

		node.value = item;
		node.prev = tail;
	}

	/**
	 * Takes a snapshot of redo log. This method can be expensive, both CPU and
	 * memory, for large structures.
	 * 
	 * @return
	 */
	public List<V> toList() {
		Node<V> current = tailRef.get();
		List<V> list = new ArrayList<>();
		while (current != null) {
			list.add(current.value);
			current = current.prev;
		}
		return Lists.reverse(list);
	}

	public Iterator<V> redo() {
		return new RedoIterator<V>(tailRef.get());
	}

	public boolean isEmpty() {
		return tailRef.get() == null;
	}

	@Override
	public Iterator<V> iterator() {
		return redo();
	}

}
