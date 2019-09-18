package pl.symentis.concurrency.mapreduce;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class ParallelMapReduce implements MapReduce {

	private static final int MAX_TASKS_PER_PHASER = 1000;

	private final ExecutorService executorService = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	@Override
	public <I, K, V> void run(Input<I> input, Mapper<I, K, V> mapper, Reducer<K, V> reducer, Output<K, V> output) {

		Phaser rootPhaser = new Phaser() {
			@Override
			protected boolean onAdvance(int phase, int registeredParties) {
				return phase == 0 && registeredParties == 0 && !input.hasNext();
			}
		};

		// map
		Map<K, Collection<V>> map = new ConcurrentHashMap<>();
		int tasksPerPhaser = 0;
		Phaser phaser = new Phaser(rootPhaser);

		while (input.hasNext()) {
			I in = input.next();

			phaser.register();

			executorService.submit(new MapperPhase<>(in, mapper, map, phaser));

			tasksPerPhaser++;
			if (tasksPerPhaser >= MAX_TASKS_PER_PHASER) {
				phaser = new Phaser(rootPhaser);
				tasksPerPhaser = 0;
			}
		}

		rootPhaser.awaitAdvance(0);

		// reduce
		Set<K> keys = map.keySet();
		for (K key : keys) {
			reducer.reduce(key, new IteratorInput<>(map.get(key).iterator()), output);
		}

	}

	@Override
	public void shutdown() {
		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			throw new WorkflowException(e);
		}
	}

	static final class MapperPhase<I, K, V> implements Runnable {

		private final I in;
		private final Mapper<I, K, V> mapper;
		private final Map<K, Collection<V>> map;
		private final Phaser phaser;

		MapperPhase(I in, Mapper<I, K, V> mapper, Map<K, Collection<V>> map, Phaser phaser) {
			this.in = in;
			this.mapper = mapper;
			this.map = map;
			this.phaser = phaser;
		}

		@Override
		public void run() {
			mapper.map(in, (k, v) -> {
				map.compute(k, (key, oldValue) -> {
					Collection<V> newValue = oldValue;
					if (newValue == null) {
						newValue = new ConcurrentLinkedQueue<>();
					}
					newValue.add(v);
					return newValue;
				});
			});
			phaser.arriveAndDeregister();
		}

	}

}
