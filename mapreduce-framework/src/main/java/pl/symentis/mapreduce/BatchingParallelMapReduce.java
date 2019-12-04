package pl.symentis.mapreduce;

import pl.symentis.mapreduce.mapper.HashMapOutput;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class BatchingParallelMapReduce implements MapReduce {

    public static class Builder {

        private int threadPoolMaxSize = Runtime.getRuntime().availableProcessors();
        private int phaserMaxTasks = 1000;
        private int batchSize = 1000;

        public Builder withThreadPoolSize(int threadPoolMaxSize) {
            this.threadPoolMaxSize = threadPoolMaxSize;
            return this;
        }

        public Builder withPhaserMaxTasks(int phaserMaxTasks) {
            this.phaserMaxTasks = phaserMaxTasks;
            return this;
        }

        public Builder withBatchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public MapReduce build() {
            return new BatchingParallelMapReduce(threadPoolMaxSize, phaserMaxTasks, batchSize);
        }
    }

    private final ExecutorService executorService;
    private final int phaserMaxTasks;
    private final int batchSize;

    public BatchingParallelMapReduce(int threadPoolMaxSize, int phaserMaxTasks, int batchSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolMaxSize);
        this.phaserMaxTasks = phaserMaxTasks;
        this.batchSize = batchSize;
    }

    @Override
    public <In, MapperKey, MapperValue, ReducerKey, ReducerValue> void run(
            Input<In> input,
            Mapper<In, MapperKey, MapperValue> mapper,
            Reducer<MapperKey, MapperValue, ReducerKey, ReducerValue> reducer,
            Output<ReducerKey, ReducerValue> output) {

        Phaser rootPhaser = new Phaser() {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                return phase == 0 && registeredParties == 0 && !input.hasNext();
            }
        };

        // map
        int tasksPerPhaser = 0;
        Phaser phaser = new Phaser(rootPhaser);

        ConcurrentLinkedDeque<Map<MapperKey, List<MapperValue>>> mapResults = new ConcurrentLinkedDeque<>();
        ArrayList<In> batch = new ArrayList<>(batchSize);

        while (input.hasNext()) {
            batch.add(input.next());

            if (batch.size() == batchSize || !input.hasNext()) {
                phaser.register();

                executorService.submit(new MapperPhase<>(new IteratorInput<>(batch.iterator()), mapper, mapResults, phaser));

                tasksPerPhaser++;
                if (tasksPerPhaser >= phaserMaxTasks) {
                    phaser = new Phaser(rootPhaser);
                    tasksPerPhaser = 0;
                }
                batch = new ArrayList<>(batchSize);
            }
        }

        rootPhaser.awaitAdvance(0);

        // merge map results
        Map<MapperKey, List<MapperValue>> map = merge(mapResults);

        // reduce
        reduce(reducer, output, map);

    }

    private <MapperKey, MapperValue, ReducerKey, ReducerValue> void reduce(
            Reducer<MapperKey, MapperValue, ReducerKey, ReducerValue> reducer, Output<ReducerKey, ReducerValue> output,
            Map<MapperKey, List<MapperValue>> map) {
        Set<MapperKey> keys = map.keySet();
        for (MapperKey key : keys) {
            reducer.reduce(key, map.get(key), output);
        }
    }

    public static  <V, K> Map<K, List<V>> merge(ConcurrentLinkedDeque<Map<K, List<V>>> mapResults) {
        return mapResults.parallelStream()
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(
                        groupingBy(
                                Map.Entry::getKey,
                                mapping(
                                        Map.Entry::getValue,
                                        reducing(
                                                new ArrayList<>(),
                                                BatchingParallelMapReduce::sum))));
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new MapReduceException(e);
        }
    }

    static final class MapperPhase<I, K, V> implements Runnable {

        private final Input<I> input;
        private final Mapper<I, K, V> mapper;
        private final Phaser phaser;
        private final Queue<Map<K, List<V>>> mapResults;

        MapperPhase(Input<I> input, Mapper<I, K, V> mapper, Queue<Map<K, List<V>>> mapResults, Phaser phaser) {
            this.input = input;
            this.mapper = mapper;
            this.mapResults = mapResults;
            this.phaser = phaser;
        }

        @Override
        public void run() {
            HashMapOutput<K, V> output = new HashMapOutput<>();
            while (input.hasNext()) {
                mapper.map(input.next(), output);
            }
            mapResults.offer(output.asMap());
            phaser.arriveAndDeregister();
        }

    }

    private static <V> List<V> sum(List<V> op1, List<V> op2) {
        ArrayList<V> vs = new ArrayList<>(op1.size() + op2.size());
        vs.addAll(op1);
        vs.addAll(op2);
        return vs;
    }

}
