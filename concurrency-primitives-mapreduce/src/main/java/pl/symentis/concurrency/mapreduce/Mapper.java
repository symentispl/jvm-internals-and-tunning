package pl.symentis.concurrency.mapreduce;

public interface Mapper<I, K, V> {

  void map(I in, Output<K, V> output);

}
