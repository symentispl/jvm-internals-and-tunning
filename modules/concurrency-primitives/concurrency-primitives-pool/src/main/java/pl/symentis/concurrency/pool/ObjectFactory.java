package pl.symentis.concurrency.pool;


/**
 * Creates new object living in a pool
 * 
 * @param <T>
 */
public interface ObjectFactory<T> {
	
	T create();

}
