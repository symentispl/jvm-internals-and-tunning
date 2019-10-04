package pl.symentis.mapreduce;

import java.util.Iterator;
import java.util.Set;

import pl.symentis.mapreduce.mapper.GuavaMultiMapOutput;

public class SequentialMapReduce implements MapReduce {

	@Override
	public <I, K, V> void run(Input<I> in, Mapper<I, K, V> mapper, Reducer<K, V> reducer, Output<K, V> output) {

		MapperOutput<K, V> mapperOutput = new GuavaMultiMapOutput<K, V>();
		while (in.hasNext()) {
			mapper.map(in.next(), mapperOutput);
		}

		Set<K> keys = mapperOutput.keys();
		for (K key : keys) {
			reducer.reduce(key, new IteratorInput<>(mapperOutput.values(key)), output);
		}
	}

	@Override
	public void shutdown() {
		
	}

	static class IteratorInput<E> implements Input<E> {

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


}
