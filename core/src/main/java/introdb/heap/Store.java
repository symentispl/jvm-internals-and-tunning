package introdb.heap;

interface Store {

	Object remove(String key);

	Object get(String key);

	/**
	 * 
	 * @param entry
	 * @throws IllegalArgumentException
	 *             when entry exceeds page size
	 */
	void put(Entry entry);

}
