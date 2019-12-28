package pl.symentis.mapreduce;

public interface MapReduce {

    <In, MapperKey, MapperValue, ReducerKey, ReducerValue> void run(
            Input<In> input,
            Mapper<In, MapperKey, MapperValue> mapper,
            Reducer<MapperKey, MapperValue, ReducerKey, ReducerValue> reducer,
            Output<ReducerKey, ReducerValue> output);

    void shutdown();

}
