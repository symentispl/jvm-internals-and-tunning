package introdb.heap;

import static java.lang.String.format;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

import introdb.api.Entry;
import introdb.api.KeyValueStorage;
import introdb.fs.Block;
import introdb.fs.BlockFile;
import introdb.record.TransientRecord;

class UnorderedHeapFile implements KeyValueStorage, Iterable<TransientRecord> {

	private static final Logger LOGGER = Logger.getLogger(UnorderedHeapFile.class.getName());

	private int lastBlockNumber = 0;

	private final BlockFile blockFile;

	UnorderedHeapFile( BlockFile blockFile ) throws IOException {
		this.blockFile = blockFile;
	}

	@Override
	public Iterator<TransientRecord> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove(Serializable key) throws IOException, ClassNotFoundException {
		var keySer = TransientRecord.serializeKey(key);
		var localLastBlockNumber = lastBlockNumber;
		for ( int currentPage = 0; currentPage <= localLastBlockNumber; currentPage++) {
			var block = blockFile.read(currentPage, () -> ByteBuffer.allocate( blockFile.blockSize() ));
			var cursor = block.cursor();
			while (cursor.hasNext()) {
				var record = cursor.next();
				if (record.isPresent() && Arrays.equals(keySer, record.key())) {
					cursor.remove();
					block.flush(blockFile);
					return record.valueAsObject();
				}
			}
		}
		return null;
	}

	@Override
	public Object get(Serializable key) throws IOException, ClassNotFoundException {

		byte[] keySer = TransientRecord.serializeKey(key);
		var localLastBlockNumber = lastBlockNumber;
		for ( int currentPage = 0; currentPage <= localLastBlockNumber; currentPage++) {
			var block = blockFile.read(currentPage, () -> ByteBuffer.allocate( blockFile.blockSize() ));
			for (var record : block) {
				if (record.isPresent()) {
					if (Arrays.equals(keySer, record.key())) {
						return record.valueAsObject();
					}
				} else if (record.isEmpty()) {
					// last record in page
					break;
				}
			}
		}
		return null;
	}

	@Override
	public void put(Entry entry) throws IOException {

		LOGGER.fine(() -> format("putting entry %s", entry));

		// create record, check if it fits in page
		var newRecord = TransientRecord.of(entry);
		if ( newRecord.size() > blockFile.blockSize()) {
			throw new IllegalArgumentException("record to large");
		}
		var localLastBlockNumber=lastBlockNumber;
		// iterate over pages
		for ( int currentPage = 0; currentPage <= localLastBlockNumber; currentPage++) {
			Block block = blockFile.read(currentPage, () -> ByteBuffer.allocate( blockFile.blockSize() ));
			var cursor = block.cursor();
			while (cursor.hasNext()) {
				var record = cursor.next();
				if (record.isPresent() && Arrays.equals(newRecord.key(), record.key())) {
					cursor.remove();
					block.flush(blockFile);
					break;

				}
			}
		}
		append(newRecord);
	}

	private void append(TransientRecord record) throws IOException {

		Block block = blockFile.read( lastBlockNumber, () -> ByteBuffer.allocate( blockFile.blockSize() ));

		if (block.remaining() < record.size()) {
			block = blockFile.create( ++lastBlockNumber, blockFile.blockSize() );
		}
		append(record, block);
	}

	private void append(TransientRecord record, Block block) throws IOException {
		LOGGER.fine(() -> format( "appending record at block %d, %s", lastBlockNumber, block));
		block.write(record);
		block.flush(blockFile);
	}

	/**
	 * Forces underlying file channel to close
	 * @throws Exception 
	 */
	public void closeForcibly() throws Exception {
		blockFile.close();
	}

}
