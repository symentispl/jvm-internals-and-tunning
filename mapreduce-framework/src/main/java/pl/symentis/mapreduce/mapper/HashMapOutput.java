package pl.symentis.mapreduce.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import pl.symentis.mapreduce.MapperOutput;

public final class HashMapOutput<K, V> implements MapperOutput<K, V> {

	private final HashMap<K, List<V>> map = new HashMap<>();

	@Override
	public void emit(K k, V v) {
		map.compute(k, (key, oldValue) -> {
			List<V> newValue = oldValue;
			if (newValue == null) {
				newValue = new ArrayList<>();
			}
			newValue.add(v);
			return newValue;
		});
	}

	@Override
	public Set<K> keys() {
		return map.keySet();
	}

	@Override
	public Iterator<V> values(K k) {
		return map.get(k).iterator();
	}
}