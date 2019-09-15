package pl.symentis.concurrency.mapreduce;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SequentialWorkflow implements Workflow {

  @Override
  public <I, K, V> void run(Input<I> in, Mapper<I, K, V> mapper, Reducer<K, V> reducer, Output<K, V> output) {

    HashMap<K, List<V>> map = new HashMap<>();
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

    Set<K> keys = map.keySet();
    for (K key : keys) {
      reducer.reduce(key, new IteratorInput<>(map.get(key).iterator()), output);
    }
  }

  static class IteratorInput<E> implements Input<E> {

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

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub
    
  }

}
