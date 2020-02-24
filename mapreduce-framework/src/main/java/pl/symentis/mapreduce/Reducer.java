package pl.symentis.mapreduce;

public interface Reducer<MapperKey, MapperValue, ReducerValue> {

	ReducerValue reduce(MapperKey k, Iterable<MapperValue> input);

}
