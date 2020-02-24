package pl.symentis.mapreduce.mapper;

import java.util.List;
import java.util.Set;

import org.eclipse.collections.impl.multimap.list.FastListMultimap;

import pl.symentis.mapreduce.MapperOutput;

public final class EclipseCollectionsMultiMapOutput<K, V> implements MapperOutput<K, V> {

    private final org.eclipse.collections.api.multimap.MutableMultimap<K, V> multimap = FastListMultimap.newMultimap();

    @Override
    public void emit(K k, V v) {
        multimap.put(k, v);
    }

    @Override
    public Set<K> keys() {
        return multimap.keySet().toSet();
    }

    @Override
    public List<V> values(K k) {
        return multimap.get(k).toList();
    }
}