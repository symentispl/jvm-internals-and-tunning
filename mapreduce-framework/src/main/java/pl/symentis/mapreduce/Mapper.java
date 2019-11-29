package pl.symentis.mapreduce;

public interface Mapper<I, K, V> {

    void map(I in, Output<K, V> output);

}
