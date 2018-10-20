package introdb.heap;

import java.nio.file.Path;

class UnorderedHeapFile implements Store{

	UnorderedHeapFile(Path path, int maxNrPages, int pageSize) {
	}

	@Override
	public void put(Entry entry) {
	}

	@Override
	public Object get(String key) {
		return null;
	}

	@Override
	public Object remove(String key) {
		return null;
	}

}
