package introdb.heap;

interface Store {

	Object remove(String key);

	Object get(String key);

	void put(Entry entry);

}
