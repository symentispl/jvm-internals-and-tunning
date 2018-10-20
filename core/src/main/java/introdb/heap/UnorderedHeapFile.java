package introdb.heap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Set;

class UnorderedHeapFile implements Store{

	private static final ThreadLocal<ByteBuffer> T_LOCAL_BUFFER = ThreadLocal.withInitial(() -> ByteBuffer.allocate(4 * 1024));

	private final int maxNrPages;
	private final int pageSize;

	private final byte[] zeroPage;

	private final FileChannel file;

	UnorderedHeapFile(Path path, int maxNrPages, int pageSize) throws IOException {
		this.file = FileChannel.open(path,
		        Set.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE));
		this.maxNrPages = maxNrPages;
		this.pageSize = pageSize;
		this.zeroPage = new byte[pageSize];
	}

	void put(Entry entry) throws IOException, ClassNotFoundException {
		Record record = Record.of(entry);

		if(record.size()>pageSize) {
			throw new RuntimeException("record to big");
		}
		
		ByteBuffer page = T_LOCAL_BUFFER.get();

		int pageNr = findAndMark(page, record.key(), record.size());

		// at this point, we get page,  
		// with suitable amount of space for record,
		// pointing to next free region

		ByteBuffer src = record.writeExternal(() -> page);

		writePage(src, pageNr);
	}
	
	Object get(Serializable key) throws IOException, ClassNotFoundException {

		// serialize key
		byte[] keySer = serializeKey(key);

		var page = T_LOCAL_BUFFER.get();

		int pageNr = 0;

		while (pageNr<maxNrPages) {

			var bytesRead = readPage(page, pageNr);

			if (bytesRead == -1) {
				return null;
			}

			if(bytesRead!=pageSize) {
				throw new IllegalStateException("corrupted heap file, cannot read page");
			}
			
			page.rewind();

			do {
				var record = Record.readExternal(() -> page);

				if (record==null) {
					break;
				}
				
				if (isRecordFound(keySer, record)) {
					return deserializeValue(record.value());
				}
			} while (page.hasRemaining());
			pageNr++;
		}
		
		throw new EOFException();
	}
	
	Object remove(Serializable key) throws IOException, ClassNotFoundException {
			// serialize key
			byte[] keySer = serializeKey(key);

			var page = T_LOCAL_BUFFER.get();

			int pageNr = 0;

			while (pageNr<maxNrPages) {

				var bytesRead = readPage(page, pageNr);

				if (bytesRead == -1) {
					return null;
				}

				if(bytesRead!=pageSize) {
					throw new IllegalStateException("corrupted heap file, cannot read page");
				}
				
				page.rewind();

				do {
					var record = Record.readExternal(() -> page);

					if (record==null) {
						break;
					}
					
					if (isRecordFound(keySer, record)) {
						return deserializeValue(record.value());
					}
				} while (page.hasRemaining());
				pageNr++;
			}
			
			throw new EOFException();
	}

	private int findAndMark(ByteBuffer page, byte[] keySer, int recordSize) throws IOException, ClassNotFoundException {

		int bytesRead = 0;
		int pageNr = 0;

		while (pageNr<maxNrPages) {
			bytesRead = readPage(page, pageNr);

			// we have reached end of file, 
			// will append record at the end
			if (bytesRead == -1) {
				return pageNr;
			}
			
			if(bytesRead!=pageSize) {
				throw new IllegalStateException("corrupted heap file, cannot read page");
			}
			
			page.rewind();

			// next we scan whole page
			do {
				page.mark();
				var record = Record.readExternal(() -> page);

				if (record == null) {
					// reset page to mark,
					// so we can write record
					// at beginning of page free space
					page.reset();
					return pageNr;
				}

				if (isRecordFound(keySer, record)) {
					// mark record as deleted,
					// and flush page
					page.reset();
					markAsRemoved(page);
					writePage(page, pageNr);
					continue;
				}

			} while (page.remaining()>=recordSize);

			pageNr++;
		}
		throw new EOFException();
	}

	private void writePage(ByteBuffer page, int pageNr) throws IOException {
		page.rewind();
		file.write(page,pageNr*pageSize);
	}

	private int readPage(ByteBuffer page, int pageOffset) throws IOException {
		clearPage(page);
		return file.read(page, pageOffset * pageSize);
	}

	private static boolean isRecordFound(byte[] keySer, Record record) {
		return record.isPresent() && Arrays.equals(record.key(), keySer);
	}

	private static void markAsRemoved(ByteBuffer page) {
		page.put((byte)Record.Mark.REMOVED.ordinal());
	}

	private static Object deserializeValue(byte[] value) throws IOException, ClassNotFoundException {
		var inputStream = new ByteArrayInputStream(value);

		try (var objectInput = new ObjectInputStream(inputStream)) {
			return objectInput.readObject();
		}
	}

	private static byte[] serializeKey(Serializable key) throws IOException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		try (var objectOutput = new ObjectOutputStream(byteArray)) {
			objectOutput.writeObject(key);
		}
		return byteArray.toByteArray();
	}

	private void clearPage(ByteBuffer page) {
		page.clear();
		page.put(zeroPage);
		page.rewind();
	}

}
