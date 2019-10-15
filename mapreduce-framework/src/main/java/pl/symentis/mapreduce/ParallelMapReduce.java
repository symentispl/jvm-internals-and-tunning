package pl.symentis.mapreduce;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.reducing;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class ParallelMapReduce implements MapReduce {
	
	public static class Builder {

		private int threadPoolMaxSize = Runtime.getRuntime().availableProcessors();
		private int phaserMaxTasks = 1000;

		public Builder withThreadPoolSize(int threadPoolMaxSize){
			this.threadPoolMaxSize = threadPoolMaxSize;
			return this;
		}
		
		public Builder withPhaserMaxTasks(int phaserMaxTasks) {
			this.phaserMaxTasks = phaserMaxTasks;
			return this;
		}

		public MapReduce build() {
			return new ParallelMapReduce(threadPoolMaxSize, phaserMaxTasks);
		}
	}

	private final ExecutorService executorService;
	private final int phaserMaxTasks;

	public ParallelMapReduce(int threadPoolMaxSize, int phaserMaxTasks ) {
		executorService  = Executors.newFixedThreadPool(threadPoolMaxSize);
		this.phaserMaxTasks = phaserMaxTasks;
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

		List<Map<K,List<V>>> slots= new ArrayList<>();
		
		while (input.hasNext()) {
			I in = input.next();

			phaser.register();

			slots.add(null);
			executorService.submit(new MapperPhase<>(in, mapper, slots, slots.size()-1, phaser));

			tasksPerPhaser++;
			if (tasksPerPhaser >= phaserMaxTasks) {
				phaser = new Phaser(rootPhaser);
				tasksPerPhaser = 0;
			}
		}

		rootPhaser.awaitAdvance(0);
		VarHandle.acquireFence();
				
		Map<K, List<V>> map = slots.stream()
				.flatMap( m -> m.entrySet().stream())
				.collect(
						groupingBy(
								Map.Entry::getKey,
								mapping( 
										Map.Entry::getValue,
										reducing(new ArrayList<>(),ParallelMapReduce::sum ))));

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
		private final Phaser phaser;
		private final List<Map<K, List<V>>> slots;
		private final int slot;

		MapperPhase(I in, Mapper<I, K, V> mapper, List<Map<K, List<V>>> slots, int slot, Phaser phaser) {
			this.in = in;
			this.mapper = mapper;
			this.slots = slots;
			this.slot = slot;
			this.phaser = phaser;
		}

		@Override
		public void run() {
			Map<K,List<V>> map = new HashMap<>();
			mapper.map(in, (k, v) -> {
				map.compute(k, (key, oldValue) -> {
					List<V> newValue = oldValue;
					if (newValue == null) {
						newValue = new ArrayList<>();
					}
					newValue.add(v);
					return newValue;
				});
			});
			slots.set(slot, map);
			VarHandle.releaseFence();
			phaser.arriveAndDeregister();
		}

	}
		
	private static <V> List<V> sum(List<V> op1, List<V> op2){
		op1.addAll(op2);
		return op2;
	}

}
