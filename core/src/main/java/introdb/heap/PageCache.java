package introdb.heap;

import static java.lang.Integer.valueOf;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class PageCache {

	private final Map<Integer,SoftReference<ByteBuffer>> pages;
	private int pageSize;

	PageCache(int maxPageNr, int pageSize) {
		this.pageSize = pageSize;
		this.pages = new HashMap<>(maxPageNr);
	}

	int get(ByteBuffer page, int pageNr) {
		SoftReference<ByteBuffer> reference = pages.get(valueOf(pageNr));
		if (reference != null) {
			ByteBuffer buffer = reference.get();
			if (buffer != null) {
				buffer.rewind();
				page.put(buffer);
				return pageSize;
			}
		}
		return -1;
	}

	public void put(ByteBuffer page, int pageNr) {
		page.rewind();
		pages.put(valueOf(pageNr),new SoftReference<>(ByteBuffer.allocate(pageSize).put(page)));		
	}

	public void invalidate(int pageNr) {
		pages.remove(valueOf(pageNr));
	}

}
