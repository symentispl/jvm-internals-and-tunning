package pl.symentis.concurrency.cache;

import java.util.HashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class ExpiringCache<K, V> {

  private final long expiryTime;
  private final TimeUnit expiryUnit;

  private final ExecutorService expiryWorker;
  private final DelayQueue<Entry> expiryQueue = new DelayQueue<>();
  private final HashMap<K, Entry> cache = new HashMap<>();
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  private volatile boolean running = true;

  ExpiringCache(long expiryTime, TimeUnit expiryUnit) {
    super();
    this.expiryTime = expiryTime;
    this.expiryUnit = expiryUnit;
    this.expiryWorker = Executors.newSingleThreadExecutor();
    expiryWorker.submit(() -> {
      while (running) {
        try {
          var entry = expiryQueue.poll(1, TimeUnit.SECONDS);
          if (entry != null) {
//            System.out.println(String.format("expiring entry %s", entry.key));
            var writeLock = lock.writeLock();
            writeLock.lock();
            try {
              cache.remove(entry.key);
            } finally {
              writeLock.unlock();
            }
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

  }

  void put(K key, V value) {
    WriteLock writeLock = lock.writeLock();
    Entry entry;
    writeLock.lock();
    try {
      entry = cache.compute(key, (k, oldValue) -> {
        if (oldValue != null) {
          oldValue.value = value;
          oldValue.updateTimestamp();
          return oldValue;
        } else {
          return new Entry(key, value);
        }
      });
    } finally {
      writeLock.unlock();
    }
    expiryQueue.remove(entry);
    expiryQueue.put(entry);
  }

  V get(K key) {
    var writeLock = lock.writeLock();
    writeLock.lock();
    try {
      var readLock = lock.readLock();
      readLock.lock();
      Entry entry = null;
      try {
        entry = cache.get(key);
      } finally {
        readLock.unlock();
      }
      if (entry != null && entry.notExpired()) {
        entry.updateTimestamp();
        expiryQueue.remove(entry);
        expiryQueue.put(entry);
        return entry.value;
      } else if (entry != null) {
        cache.remove(key);
      }
    } finally {
      writeLock.unlock();
    }
    return null;
  }

  void shutdown() {
    running = false;
    expiryWorker.shutdown();
    try {
      expiryWorker.awaitTermination(2, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  final class Entry implements Delayed {
    final K key;
    volatile V value;
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
//      System.out.println(String.format("update timestamp for key %s is %d", key, timestamp));
    }

    @Override
    public int compareTo(Delayed o) {
//      System.out.println(String.format("compareTo(%s,%s)", this, o));
      @SuppressWarnings("unchecked")
      Entry entry = (Entry) o;
      if (this.timestamp < entry.timestamp) {
        return -1;
      }

      if (this.timestamp > entry.timestamp) {
        return 1;
      }

//      System.out.println("kurwa");
      return 0;
    }

    @Override
    public long getDelay(TimeUnit unit) {
      long delay = unit.convert(expiryUnit.toMillis(expiryTime) - (System.currentTimeMillis() - timestamp),
          TimeUnit.MILLISECONDS);

//      System.out.println(String.format("delay for key %s is %d", key, delay));

      return delay;
    }

    @Override
    public String toString() {
      return String.format("Entry [key=%s, value=%s, timestamp=%s]", key, value, timestamp);
    }

  }

  public static void main(String[] args) throws Exception {
    ExpiringCache<String, String> cache = new ExpiringCache<String, String>(5, TimeUnit.SECONDS);
    System.out.println("putting Hello");
    cache.put("Hello", "World!!!");
    Thread.sleep(TimeUnit.SECONDS.toMillis(4));
    System.out.println(cache.get("Hello"));
    cache.put("Witaj", "Świecie!!!");
    System.out.println(cache.get("Hello"));
  }
}
