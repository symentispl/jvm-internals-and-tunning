package introdb.heap;

import introdb.api.Entry;
import introdb.api.KeyValueStorage;

import java.io.Serializable;
import java.nio.file.Path;

class UnorderedHeapFile implements KeyValueStorage
{

	UnorderedHeapFile(Path path, int maxBlockNr, int blockSize) {
	}

	@Override
	public void put( Entry entry) {
	}

	@Override
	public Object get(Serializable key) {
		return null;
	}

	@Override
	public Object remove(Serializable key) {
		return null;
	}

	@Override
	public void closeForcibly(){
	}

}
