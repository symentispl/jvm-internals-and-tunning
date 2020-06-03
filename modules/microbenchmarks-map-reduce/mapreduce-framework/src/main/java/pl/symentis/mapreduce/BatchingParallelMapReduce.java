package pl.symentis.mapreduce;

import pl.symentis.mapreduce.mapper.HashMapOutput;

import java.util.*;
import java.util.concurrent.*;

import static java.util.stream.Collectors.*;

public class BatchingParallelMapReduce implements MapReduce {

    public static class Builder {

        private int threadPoolMaxSize = Runtime.getRuntime().availableProcessors();
        private int phaserMaxTasks = 1000;
        private int batchSize = 10000;
		private boolean forkJoin;

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
        
        public Builder withForkJoin(boolean forkJoin) {
        	this.forkJoin = forkJoin;
        	return this;
        }

        public MapReduce build() {
            return new BatchingParallelMapReduce(threadPoolMaxSize, phaserMaxTasks, batchSize, forkJoin);
        }
    }

    private final ExecutorService executorService;
    private final int phaserMaxTasks;
    private final int batchSize;

    public BatchingParallelMapReduce(int threadPoolMaxSize, int phaserMaxTasks, int batchSize, boolean forkJoin ) {
    	if(forkJoin) {
            this.executorService  = ForkJoinPool.commonPool();
    	} else {
    		this.executorService = Executors.newFixedThreadPool(threadPoolMaxSize);    		
    	}
        this.phaserMaxTasks = phaserMaxTasks;
        this.batchSize = batchSize;
    }

    @Override
    public <In, MK, MV, RV> 
    void run(Input<In> input, MapReduceJob<In, MK, MV, RV> mapReduceJob,Output<MK, RV> output) {

        Phaser rootPhaser = new Phaser() {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                return phase == 0 && registeredParties == 0 && !input.hasNext();
            }
        };

        // map
        int tasksPerPhaser = 0;
        Phaser phaser = new Phaser(rootPhaser);

        ConcurrentLinkedDeque<Map<MK, List<MV>>> mapResults = new ConcurrentLinkedDeque<>();
        ArrayList<In> batch = new ArrayList<>(batchSize);

        while (input.hasNext()) {
            batch.add(input.next());

            if (batch.size() == batchSize || !input.hasNext()) {
                phaser.register();

                executorService
                        .submit(new MapperPhase<>(new IteratorInput<>(batch.iterator()), mapReduceJob.mapper(), mapResults, phaser));

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
        ConcurrentMap<MK, RV> mergeAndReduce = mergeAndReduce(mapResults, mapReduceJob);

        for(Map.Entry<MK, RV> entry: mergeAndReduce.entrySet()) {
        	output.emit(entry.getKey(), entry.getValue());
        }

    }

    static <In,MK, MV, RK, RV> ConcurrentMap<MK, RV> mergeAndReduce(ConcurrentLinkedDeque<Map<MK, List<MV>>> mapResults,
            MapReduceJob<In, MK, MV, RV> mapReduceJob) {
         return mapResults.parallelStream()
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(
                    groupingByConcurrent(
                        Map.Entry::getKey,
                        mapping( entry -> {
                            return mapReduceJob.reducer().reduce(entry.getKey(), entry.getValue());
                        },
                        reducing(
                            mapReduceJob.identity(),
                            mapReduceJob.rereducer()))));
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

}
