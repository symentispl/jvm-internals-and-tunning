package pl.symentis.concurrency.mapreduce;

public interface Output<K, V> {

  void emit(K k, V v);

}
