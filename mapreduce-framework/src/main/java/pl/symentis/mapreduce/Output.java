package pl.symentis.mapreduce;

public interface Output<K, V> {

    void emit(K k, V v);

}
