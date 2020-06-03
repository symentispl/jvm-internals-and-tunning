package pl.symentis.mapreduce.mapper;

import pl.symentis.mapreduce.MapperOutput;

import java.util.*;

public final class HashMapOutput<K, V> implements MapperOutput<K, V> {

    private final HashMap<K, List<V>> map = new HashMap<>();

    @Override
    public void emit(K k, V v) {
        map.computeIfAbsent(k, key -> new ArrayList<V>()).add(v);
    }

    @Override
    public Set<K> keys() {
        return map.keySet();
    }

    @Override
    public List<V> values(K k) {
        return map.get(k);
    }

    public Map<K, List<V>> asMap() {
        return map;
    }
}