package pl.symentis.mapreduce;

public interface Reducer<K, V> {

  void reduce(K k, Input<V> input, Output<K, V> output);

}
