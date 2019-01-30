package pl.symentis.concurrency.cache;

import static java.lang.String.format;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Cache<K, V> {

  private final int maxSize;
  private final long expiryTime;
  private final TimeUnit expiryUnit;

  private final ExecutorService expiryWorker;
  private final DelayQueue<Entry> expiryQueue = new DelayQueue<>();
  private final ConcurrentHashMap<K, Entry> cache = new ConcurrentHashMap<>();

  Cache(int maxSize, long expiryTime, TimeUnit expiryUnit) {
    super();
    this.maxSize = maxSize;
    this.expiryTime = expiryTime;
    this.expiryUnit = expiryUnit;
    this.expiryWorker = Executors.newSingleThreadExecutor();
    expiryWorker.submit(() -> {
      while (true) {
        Entry entry = expiryQueue.poll(1, TimeUnit.SECONDS);
        if (entry != null) {
          cache.compute(entry.key, (key, oldValue) -> {
            if (oldValue == entry && entry.expired()) {
              System.out.println(format("expiring entry [%s,%s]", entry.key, entry.value));
              return null;
            }
            return oldValue;
          });
        }
      }
    });

  }

  void put(K k, V v) {
    Entry entry = new Entry(k, v);
    cache.put(k, entry);
    expiryQueue.put(entry);
  }

  V get(K k) {
    Entry entry = cache.get(k);
    if (entry != null && entry.notExpired()) {
      entry.updateTimestamp();
      return entry.value;
    }
    return null;
  }

  final class Entry implements Delayed {
    final K key;
    final V value;
    volatile long timestamp = System.currentTimeMillis();

    Entry(K k, V v) {
      this.key = k;
      this.value = v;
    }

    public boolean expired() {
      return System.currentTimeMillis() - timestamp > expiryUnit.toMillis(expiryTime);
    }

    public boolean notExpired() {
      return !expired();
    }

    void updateTimestamp() {
      timestamp = System.currentTimeMillis();
      System.out.println(String.format("update timestamp for key %s is %d", key, timestamp));
    }

    @Override
    public int compareTo(Delayed o) {
      @SuppressWarnings("unchecked")
      Entry entry = (Entry) o;
      if (this.timestamp < entry.timestamp) {
        return -1;
      }

      if (this.timestamp > this.timestamp) {
        return 1;
      }

      return 0;
    }

    @Override
    public long getDelay(TimeUnit unit) {
      long delay = unit.convert(expiryUnit.toMillis(expiryTime) - (System.currentTimeMillis() - timestamp),
          TimeUnit.MILLISECONDS);

      System.out.println(String.format("delay for key %s is %d", key, delay));

      return delay;
    }
  }

  public static void main(String[] args) throws Exception {
    Cache<String, String> cache = new Cache<String, String>(16, 5, TimeUnit.SECONDS);
    cache.put("Hello", "World!!!");
    System.out.println(cache.get("Hello"));
    Thread.sleep(TimeUnit.SECONDS.toMillis(2));
    cache.put("Witaj", "Świecie!!!");
    System.out.println(cache.get("Hello"));
  }
}
