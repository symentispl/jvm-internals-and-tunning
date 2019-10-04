package pl.symentis.mapreduce;

public interface MapReduce {

  <I, K, V> void run(Input<I> in, Mapper<I, K, V> mapper, Reducer<K, V> reducer, Output<K, V> output);

  void shutdown();

}
