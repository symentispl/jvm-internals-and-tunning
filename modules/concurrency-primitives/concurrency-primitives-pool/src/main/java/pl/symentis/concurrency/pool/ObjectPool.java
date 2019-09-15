package pl.symentis.concurrency.pool;


import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectPool<T> {

	private final ObjectFactory<T> fcty;
	private final ObjectValidator<T> validator;
	private final int maxPoolSize;

	private final ArrayBlockingQueue<T> objectPool;
	private final ConcurrentLinkedQueue<CompletableFuture<T>> uncompletedTasks = new ConcurrentLinkedQueue<>();

	private final AtomicInteger poolSize = new AtomicInteger(0);

	public ObjectPool(ObjectFactory<T> fcty, ObjectValidator<T> validator) {
		this(fcty, validator, 25);
	}

	public ObjectPool(ObjectFactory<T> fcty, ObjectValidator<T> validator, int maxPoolSize) {
		this.fcty = fcty;
		this.validator = validator;
		this.maxPoolSize = maxPoolSize;
		this.objectPool = new ArrayBlockingQueue<>(maxPoolSize);
	}

	/**
	 * When there is object in pool returns completed future, if not, future will be
	 * completed when object is returned to the pool.
	 * 
	 * @return
	 */
	public CompletableFuture<T> borrowObject() {

		// fast path, in case there is object in pool, return it immediately
		T object = objectPool.poll();
		if (object != null) {
			return completedFuture(object);
		}

		if (poolSize.get() == maxPoolSize) {
			return uncompletedRequest();
		}

		int claimed;
		int next;
		do {
			claimed = poolSize.get();
			next = claimed + 1;
			if (next > maxPoolSize) { // when competing thread reached max first, wait
				return uncompletedRequest();
			}
		} while (!poolSize.compareAndSet(claimed, next));

		return completedFuture(fcty.create());
	}

	public void returnObject(T object) {
		if (validator.validate(object)) {
			// piggyback, on release, check if there is any task waiting for object
			CompletableFuture<T> future = uncompletedTasks.poll();
			if (future != null) {
				future.complete(object);
			} else {
				objectPool.offer(object);
			}
		} else {
			poolSize.decrementAndGet();
		}
	}

	public void shutdown() throws InterruptedException {
	}

	public int getPoolSize() {
		return poolSize.get();
	}

	public int getInUse() {
		return poolSize.get() - objectPool.size();
	}

	private CompletableFuture<T> uncompletedRequest() {
		CompletableFuture<T> req = new CompletableFuture<T>();
		uncompletedTasks.add(req);
		return req;
	}

}
