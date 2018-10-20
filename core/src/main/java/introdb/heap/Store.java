package introdb.heap;

import java.io.IOException;
import java.io.Serializable;

interface Store {

	Object remove(Serializable key) throws IOException, ClassNotFoundException;

	Object get(Serializable key) throws IOException, ClassNotFoundException;

	/**
	 * 
	 * @param entry
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws IllegalArgumentException
	 *             when entry exceeds page size
	 */
	void put(Entry entry) throws IOException, ClassNotFoundException;

}
