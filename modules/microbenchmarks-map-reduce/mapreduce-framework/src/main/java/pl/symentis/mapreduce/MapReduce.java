package pl.symentis.mapreduce;

public interface MapReduce {

	<In, MapperKey, MapperValue, ReducerValue> void run(
			Input<In> input,
			MapReduceJob<In, MapperKey, MapperValue, ReducerValue> job, 
			Output<MapperKey, ReducerValue> output);

	void shutdown();

}
