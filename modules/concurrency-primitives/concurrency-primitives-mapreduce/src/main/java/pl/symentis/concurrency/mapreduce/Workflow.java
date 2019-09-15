package pl.symentis.concurrency.mapreduce;

public interface Workflow {

  <I, K, V> void run(Input<I> in, Mapper<I, K, V> mapper, Reducer<K, V> reducer, Output<K, V> output);

  void shutdown();

}
