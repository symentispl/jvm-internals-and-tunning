package pl.symentis.concurrency.mapreduce;

public interface Reducer<K, V> {

  void reduce(K k, Input<V> input, Output<K, V> output);

}
