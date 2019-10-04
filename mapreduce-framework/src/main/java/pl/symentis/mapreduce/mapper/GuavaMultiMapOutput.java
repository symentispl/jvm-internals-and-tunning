package pl.symentis.mapreduce.mapper;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;

import pl.symentis.mapreduce.MapperOutput;

public final class GuavaMultiMapOutput<K, V> implements MapperOutput<K, V> {

	private final com.google.common.collect.Multimap<K, V> multimap = ArrayListMultimap.create();

	@Override
	public void emit(K k, V v) {
		multimap.put(k, v);
	}

	@Override
	public Set<K> keys() {
		return multimap.keySet();
	}

	@Override
	public Iterator<V> values(K k) {
		return multimap.get(k).iterator();
	}
}