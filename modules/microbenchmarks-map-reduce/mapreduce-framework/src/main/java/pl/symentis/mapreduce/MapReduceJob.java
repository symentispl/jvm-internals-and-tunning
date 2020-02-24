package pl.symentis.mapreduce;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;

public class MapReduceJob<In, MapperKey, MapperValue, ReducerValue> {

	private final Mapper<In, MapperKey, MapperValue> mapper;
	private final Reducer<MapperKey, MapperValue, ReducerValue> reducer;
	private final Supplier<ReducerValue> identitySupplier;
	private final BinaryOperator<ReducerValue> rereducer;

	public MapReduceJob(Mapper<In, MapperKey, MapperValue> mapper,
			Reducer<MapperKey, MapperValue, ReducerValue> reducer, 
			Supplier<ReducerValue> identitySupplier,
			BinaryOperator<ReducerValue> rereducer) {
		super();
		this.mapper = mapper;
		this.reducer = reducer;
		this.identitySupplier = identitySupplier;
		this.rereducer = rereducer;
	}

	Mapper<In, MapperKey, MapperValue> mapper() {
		return mapper;
	}

	Reducer<MapperKey, MapperValue, ReducerValue> reducer() {
		return reducer;
	}

	ReducerValue identity() {
		return identitySupplier.get();
	}

	BinaryOperator<ReducerValue> rereducer() {
		return rereducer;
	}

}
