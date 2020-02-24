package pl.symentis.mapreduce;

import java.util.List;
import java.util.Set;

public interface Values<K, V> {

    Set<K> keys();

    List<V> values(K k);

}
