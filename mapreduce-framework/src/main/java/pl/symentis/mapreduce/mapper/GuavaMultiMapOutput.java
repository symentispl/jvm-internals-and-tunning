package pl.symentis.mapreduce.mapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;

import pl.symentis.mapreduce.MapperOutput;

import java.util.List;
import java.util.Set;

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
	public List<V> values(K k) {
		return ImmutableList.copyOf(multimap.get(k));
	}
}