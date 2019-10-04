package pl.symentis.mapreduce;

import java.util.Iterator;
import java.util.Set;

public interface Values<K, V> {

	Set<K> keys();

	Iterator<V> values(K k);

}
