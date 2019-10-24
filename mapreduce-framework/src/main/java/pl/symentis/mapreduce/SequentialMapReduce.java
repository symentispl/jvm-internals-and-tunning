package pl.symentis.mapreduce;

import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import pl.symentis.mapreduce.mapper.HashMapOutput;

public class SequentialMapReduce implements MapReduce {

	public static class Builder {

		@SuppressWarnings("rawtypes")
		private Class<? extends MapperOutput> mapperOutputClass = HashMapOutput.class;

		public Builder withMapperOutput(Class<? extends MapperOutput<?, ?>> mapperOutputClass) {
			Objects.nonNull(mapperOutputClass);
			this.mapperOutputClass = mapperOutputClass;
			return this;
		}

		public MapReduce build() {

			@SuppressWarnings("rawtypes")
			Supplier<? extends MapperOutput> supplier = () -> {
				try {
					return mapperOutputClass.getConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new IllegalArgumentException(format("cannot instatiate mapper output class %s", mapperOutputClass), e);
				}
			};

			return new SequentialMapReduce(supplier);
		}

	}

	@SuppressWarnings("rawtypes")
	private Supplier<? extends MapperOutput> mapperOutputSupplier;

	@SuppressWarnings("rawtypes")
	private SequentialMapReduce(Supplier<? extends MapperOutput> mapperOutputSupplier) {
		this.mapperOutputSupplier = mapperOutputSupplier;
	}

	@Override
	public <I, K, V> void run(Input<I> in, Mapper<I, K, V> mapper, Reducer<K, V> reducer, Output<K, V> output) {

		@SuppressWarnings("unchecked")
		MapperOutput<K, V> mapperOutput = mapperOutputSupplier.get();

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
		;
	}

	static class IteratorInput<E> implements Input<E> {

		private final Iterator<E> iterator;

		IteratorInput(Iterator<E> iterator) {
			Objects.nonNull(iterator);
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
