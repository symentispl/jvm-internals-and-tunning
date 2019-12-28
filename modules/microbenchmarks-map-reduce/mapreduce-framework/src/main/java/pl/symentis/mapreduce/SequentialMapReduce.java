package pl.symentis.mapreduce;

import pl.symentis.mapreduce.mapper.HashMapOutput;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static java.lang.String.format;

public class SequentialMapReduce implements MapReduce {

    public static class Builder {

        @SuppressWarnings("rawtypes")
        private Class<? extends MapperOutput> mapperOutputClass = HashMapOutput.class;

        public Builder withMapperOutput(Class<? extends MapperOutput<?, ?>> mapperOutputClass) {
            Objects.nonNull(mapperOutputClass);
            this.mapperOutputClass = mapperOutputClass;
            return this;
        }

        public MapReduce build() {

            @SuppressWarnings("rawtypes")
            Supplier<? extends MapperOutput> supplier = () -> {
                try {
                    return mapperOutputClass.getConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    throw new IllegalArgumentException(format("cannot instatiate mapper output class %s", mapperOutputClass), e);
                }
            };

            return new SequentialMapReduce(supplier);
        }

    }

    @SuppressWarnings("rawtypes")
    private Supplier<? extends MapperOutput> mapperOutputSupplier;

    @SuppressWarnings("rawtypes")
    private SequentialMapReduce(Supplier<? extends MapperOutput> mapperOutputSupplier) {
        this.mapperOutputSupplier = mapperOutputSupplier;
    }

    @Override
    public <In, MapperKey, MapperValue, ReducerKey, ReducerValue> void run(
            Input<In> input,
            Mapper<In, MapperKey, MapperValue> mapper,
            Reducer<MapperKey, MapperValue, ReducerKey, ReducerValue> reducer,
            Output<ReducerKey, ReducerValue> output) {

        @SuppressWarnings("unchecked")
        MapperOutput<MapperKey, MapperValue> mapperOutput = mapperOutputSupplier.get();

        while (input.hasNext()) {
            mapper.map(input.next(), mapperOutput);
        }

        Set<MapperKey> keys = mapperOutput.keys();
        for (MapperKey key : keys) {
            reducer.reduce(key, () -> mapperOutput.values(key), output);
        }
    }

    @Override
    public void shutdown() {
        ;
    }

}
