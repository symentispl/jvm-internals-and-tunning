package pl.symentis.concurrency.cache;

import java.util.HashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpiringCache<K, V> {

	private static final Logger LOG = LoggerFactory.getLogger(ExpiringCache.class);

	private final long expiryTime;
	private final TimeUnit expiryUnit;

	private final HashMap<K, Entry> cache = new HashMap<>();

	private final ExecutorService expiryWorker;
	private final DelayQueue<Entry> expiryQueue = new DelayQueue<>();

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
					ExpiringCache<K, V>.Entry entry = expiryQueue.poll(1, TimeUnit.SECONDS);
					if (entry != null) {
						WriteLock writeLock = lock.writeLock();
						writeLock.lock();
						try {
							cache.remove(entry.key);
						} finally {
							writeLock.unlock();
						}
					}
				} catch (InterruptedException e) {
					LOG.error("expired entry worker interuppted", e);
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
		WriteLock writeLock = lock.writeLock();
		writeLock.lock();
		try {
			ReadLock readLock = lock.readLock();
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
		}

		@Override
		public int compareTo(Delayed o) {
			@SuppressWarnings("unchecked")
			Entry entry = (Entry) o;
			if (this.timestamp < entry.timestamp) {
				return -1;
			}

			if (this.timestamp > entry.timestamp) {
				return 1;
			}
			return 0;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(expiryUnit.toMillis(expiryTime) - (System.currentTimeMillis() - timestamp),
					TimeUnit.MILLISECONDS);
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
