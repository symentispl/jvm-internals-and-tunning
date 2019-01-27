package pl.symentis.concurreny.pool;


public interface ObjectValidator<T> {

	/**
	 * Returns true, when object is in a state that can be returned to a pool, like
	 * lock is not owned by any thread, connection is valid, etc.
	 * 
	 * @param object
	 * @return
	 */
	boolean validate(T object);

}
