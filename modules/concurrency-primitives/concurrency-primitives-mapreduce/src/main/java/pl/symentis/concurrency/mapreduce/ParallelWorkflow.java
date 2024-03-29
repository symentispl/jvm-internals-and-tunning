package pl.symentis.concurrency.mapreduce;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class ParallelWorkflow implements Workflow {

  private static final int MAX_TASKS_PER_PHASER = 64;

  private final ExecutorService executorService = newFixedThreadPool(getRuntime().availableProcessors());

  @Override
  public <I, K, V> void run(Input<I> input, Mapper<I, K, V> mapper, Reducer<K, V> reducer, Output<K, V> output) {

    var rootPhaser = new Phaser() {
      @Override
      protected boolean onAdvance(int phase, int registeredParties) {
        return phase == 0 && registeredParties == 0 && !input.hasNext();
      }
    };

    // map
    Map<K, Collection<V>> map = new ConcurrentHashMap<>();
    var tasksPerPhaser = 0;
    var phaser = new Phaser(rootPhaser);
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
    var keys = map.keySet();
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

  static final class IteratorInput<E> implements Input<E> {

    private final Iterator<E> iterator;

    IteratorInput(Iterator<E> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public E next() {
      return iterator.next();
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
