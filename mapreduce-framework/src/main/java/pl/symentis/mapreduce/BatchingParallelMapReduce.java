package pl.symentis.mapreduce;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.reducing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class BatchingParallelMapReduce implements MapReduce {

	public static class Builder {

		private int threadPoolMaxSize = Runtime.getRuntime().availableProcessors();
		private int phaserMaxTasks = 1000;

		public Builder withThreadPoolSize(int threadPoolMaxSize) {
			this.threadPoolMaxSize = threadPoolMaxSize;
			return this;
		}

		public Builder withPhaserMaxTasks(int phaserMaxTasks) {
			this.phaserMaxTasks = phaserMaxTasks;
			return this;
		}

		public MapReduce build() {
			return new BatchingParallelMapReduce(threadPoolMaxSize, phaserMaxTasks);
		}
	}

	private final ExecutorService executorService;
	private final int phaserMaxTasks;
	private final int batchSize;

	public BatchingParallelMapReduce(int threadPoolMaxSize, int phaserMaxTasks) {
		this.executorService = Executors.newFixedThreadPool(threadPoolMaxSize);
		this.phaserMaxTasks = phaserMaxTasks;
		this.batchSize = 1000;
	}

	@Override
	public <I, K, V> void run(Input<I> input, Mapper<I, K, V> mapper, Reducer<K, V> reducer, Output<K, V> output) {

		Phaser rootPhaser = new Phaser() {
			@Override
			protected boolean onAdvance(int phase, int registeredParties) {
				return phase == 0 && registeredParties == 0 && !input.hasNext();
			}
		};

		// map
		int tasksPerPhaser = 0;
		Phaser phaser = new Phaser(rootPhaser);

		ConcurrentLinkedDeque<Map<K, List<V>>> tasks = new ConcurrentLinkedDeque<Map<K, List<V>>>();
		ArrayList<I> buffer = new ArrayList<>(batchSize);

		while (input.hasNext()) {
			buffer.add(input.next());

			if (buffer.size() == batchSize) {
				phaser.register();

				executorService.submit(new MapperPhase<>(new IteratorInput<>(buffer.iterator()), mapper, tasks, phaser));

				tasksPerPhaser++;
				if (tasksPerPhaser >= phaserMaxTasks) {
					phaser = new Phaser(rootPhaser);
					tasksPerPhaser = 0;
				}
				buffer = new ArrayList<>(batchSize);
			}
		}

		rootPhaser.awaitAdvance(0);

		Map<K, List<V>> map = tasks.stream().flatMap(m -> m.entrySet().stream()).collect(groupingBy(Map.Entry::getKey,
				mapping(Map.Entry::getValue, reducing(new ArrayList<>(), BatchingParallelMapReduce::sum))));

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

		private final Input<I> in;
		private final Mapper<I, K, V> mapper;
		private final Phaser phaser;
		private final Queue<Map<K, List<V>>> slots;

		MapperPhase(Input<I> in, Mapper<I, K, V> mapper, Queue<Map<K, List<V>>> slots, Phaser phaser) {
			this.in = in;
			this.mapper = mapper;
			this.slots = slots;
			this.phaser = phaser;
		}

		@Override
		public void run() {
			Map<K, List<V>> map = new HashMap<>();
			while (in.hasNext()) {
				mapper.map(in.next(), (k, v) -> {
					map.compute(k, (key, oldValue) -> {
						List<V> newValue = oldValue;
						if (newValue == null) {
							newValue = new ArrayList<>();
						}
						newValue.add(v);
						return newValue;
					});
				});
			}
			slots.offer(map);
			phaser.arriveAndDeregister();
		}

	}

	private static <V> List<V> sum(List<V> op1, List<V> op2) {
		op1.addAll(op2);
		return op2;
	}

}
