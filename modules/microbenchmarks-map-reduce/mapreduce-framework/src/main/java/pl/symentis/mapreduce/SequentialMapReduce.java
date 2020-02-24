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
    public <In, MK, MV, RV> void run(
            Input<In> input,
            MapReduceJob<In, MK, MV, RV> mapReduceJob,
            Output<MK, RV> output) {

        @SuppressWarnings("unchecked")
        MapperOutput<MK, MV> mapperOutput = mapperOutputSupplier.get();

        while (input.hasNext()) {
            mapReduceJob.mapper().map(input.next(), mapperOutput);
        }

        Set<MK> keys = mapperOutput.keys();
        for (MK key : keys) {
            output.emit(key,mapReduceJob.reducer().reduce(key, mapperOutput.values(key)));
        }
    }

    @Override
    public void shutdown() {
        ;
    }

}
