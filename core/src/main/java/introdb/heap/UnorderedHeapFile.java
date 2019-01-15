package introdb.heap;

import java.io.Serializable;
import java.nio.file.Path;

class UnorderedHeapFile implements Store{

	UnorderedHeapFile(Path path, int maxNrPages, int pageSize) {
	}

	@Override
	public void put(Entry entry) {
	}
	
	@Override
	public Object get(Serializable key) {
		return null;
	}
	
	public Object remove(Serializable key) {
		return null;
	}

}

