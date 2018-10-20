package introdb.heap;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

class UnorderedHeapFile implements Store{

	UnorderedHeapFile(Path path, int maxNrPages, int pageSize) {
	}

	@Override
	public void put(Entry entry) throws IOException, ClassNotFoundException {
	}
	
	@Override
	public Object get(Serializable key) throws IOException, ClassNotFoundException {
		return null;
	}
	
	public Object remove(Serializable key) throws IOException, ClassNotFoundException {
		return null;
	}

}

