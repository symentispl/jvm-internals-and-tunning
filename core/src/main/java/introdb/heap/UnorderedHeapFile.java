package introdb.heap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import introdb.heap.Record.Mark;

class UnorderedHeapFile implements Store, Iterable<Record> {

	private static final ThreadLocal<ByteBuffer> T_LOCAL_BUFFER = ThreadLocal.withInitial(() -> ByteBuffer.allocate(4 * 1024));
	
	private final PageCache pagecache;
	private final int maxNrPages;
	private final int pageSize;
 
	private final byte[] zeroPage;
	private ByteBuffer lastPage;
	private int lastPageNumber=-1;

	private final FileChannel file;

	UnorderedHeapFile(Path path, int maxNrPages, int pageSize) throws IOException {
		this.file = FileChannel.open(path,
		        Set.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE));
		this.pagecache = new PageCache(maxNrPages,pageSize);
		this.maxNrPages = maxNrPages;
		this.pageSize = pageSize;
		this.zeroPage = new byte[pageSize];
	}

	@Override
	public void put(Entry entry) {
		
		assertTooManyPages();
		
		var newRecord = Record.of(entry);

		assertRecordSize(newRecord);

		var iterator = cursor();
		while(iterator.hasNext()){
			var record = iterator.next();
			if(Arrays.equals(newRecord.key(),record.key())){
				iterator.remove();
				break;
			}
		}

		if((lastPage!=null && lastPage.remaining()<newRecord.size()) || lastPage==null  ){
			lastPage = ByteBuffer.allocate(pageSize);
			lastPageNumber++;			
		}
		
		var src = newRecord.write(() -> lastPage);

		writePage(src, lastPageNumber);
	}

	@Override
	public Object get(Serializable key) {

		// serialize key
		var keySer = serializeKey(key);
		
		var iterator = cursor();
		while(iterator.hasNext()){
			var record = iterator.next();
			if(Arrays.equals(keySer, record.key())){
				return deserializeValue(record.value());
			}
		}
		return null;
	}

	public Object remove(Serializable key) {
		// serialize key
		var keySer = serializeKey(key);

		var iterator = cursor();
		while(iterator.hasNext()){
			var record = iterator.next();
			if(Arrays.equals(keySer, record.key())){
				var value = deserializeValue(record.value());
				iterator.remove();
				return value;
			}
		}
		return null;
	}
	
	private void writePage(ByteBuffer page, int pageNr) throws IOException {
		int position = page.position();
		page.rewind();
		file.write(page, pageNr * pageSize);
		page.position(position);
		pagecache.remove(pageNr);
	}

	private int readPage(ByteBuffer page, int pageNr) {
		
		clearPage(page);
		if(pagecache.get(page, pageNr)!=-1) {
			return pageSize;
		}
		try {
			var bytesRead = file.read(page, pageNr * pageSize);
			if(bytesRead!=-1) {
				pagecache.put(page,pageNr);
			}
			return bytesRead;
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
	private void assertRecordSize(Record newRecord) {
		if (newRecord.size() > pageSize) {
			throw new IllegalArgumentException("entry to big");
		}
	}

	private void assertTooManyPages() {
		if(lastPageNumber>=maxNrPages) {
			throw new TooManyPages();
		}
	}

	private static Object deserializeValue(byte[] value) throws IOException, ClassNotFoundException {
		try (var inputStream = new ByteArrayInputStream(value)) {

			try (var objectInput = new ObjectInputStream(inputStream)) {
				return objectInput.readObject();
			}
		}
	}

	private static byte[] serializeKey(Serializable key) throws IOException {
		try (var byteArray = new ByteArrayOutputStream()) {
			try (var objectOutput = new ObjectOutputStream(byteArray)) {
				objectOutput.writeObject(key);
			}
			return byteArray.toByteArray();
		}
	}

	private void clearPage(ByteBuffer page) {
		page.clear();
		page.put(zeroPage);
		page.rewind();
	}

	private ByteBuffer zeroPage() {
		ByteBuffer page = T_LOCAL_BUFFER.get();
		clearPage(page);
		return page;
	}

	class Cursor implements Iterator<Record> {

		int pageNr = 0;
		ByteBuffer page = null;
		boolean hasNext = false;
		private int inPagePosition = -1;

		@Override
		public boolean hasNext() {
			if (!hasNext) {
				inPagePosition = -1;
				do{
					if (page == null) {
						page = zeroPage();
						var bytesRead = readPage(page, pageNr);
						if (bytesRead == -1) {
							return hasNext = false;
						}
						page.rewind();
					}

					byte mark = 0;
					do {
						mark = page.get();
						if (Mark.isPresent(mark)) {
							return hasNext = true;
						}
						if (Mark.isRemoved(mark)) {
							skip();
						}
					} while (!Mark.isEmpty(mark) || !page.hasRemaining());

					page = null;
					pageNr++;

				} while(true);
			}
			return hasNext;
		}


		@Override
		public Record next() {
			if (hasNext || hasNext()) {
				hasNext = false;
				inPagePosition = page.position()-1;
				return record();
			}
			throw new NoSuchElementException();
		}
		
		@Override
		public void remove() {
			if(inPagePosition<0) {
				throw new IllegalStateException("next() method has not yet been called, or the remove() method has already been called");
			}
			page.put(inPagePosition, Mark.REMOVED.mark());
			try {
				writePage(page, pageNr);
				//force page refresh
				lastPage = null;
				inPagePosition = -1;
			} catch (IOException e) {
				throw new IOError(e);
			}
		}


		Optional<ByteBuffer> page() {
			return Optional.ofNullable(page);
		}

		private Record record() {
			return Record.read(() -> page);
		}

		private void skip() {
			Record.skip(() -> page);
		}

	}

	Cursor cursor() {
		return new Cursor();
	}

	@Override
	public Iterator<Record> iterator() {
		return cursor();
	}

}

