package introdb.heap;

import introdb.api.Entry;
import introdb.api.KeyValueStorage;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import introdb.api.Entry;
import introdb.api.KeyValueStorage;
import introdb.fs.Block;
import introdb.fs.BlockFile;
import introdb.record.TransientRecord;

import static java.lang.String.format;

class UnorderedHeapFile implements KeyValueStorage, Iterable<TransientRecord> {

	private static final Logger LOGGER = Logger.getLogger(UnorderedHeapFile.class.getName());

	private final int maxBlockNumber;
	private final int blockSize;

	private int lastBlockNumber = 0;

	private final FileChannel file;
	private final BlockFile blockFile;

	UnorderedHeapFile( Path path, int maxBlockNumber, int blockSize ) throws IOException {
		this.file = FileChannel.open(path,
				Set.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE));
		blockFile = new BlockFile( file, blockSize );
		this.maxBlockNumber = maxBlockNumber;
		this.blockSize = blockSize;
	}

	@Override
	public Iterator<TransientRecord> iterator() {
		return null;
	}

	@Override
	public Object remove(Serializable key) throws IOException, ClassNotFoundException {
		byte[] keySer = TransientRecord.serializeKey(key);
		var localLastBlockNumber = lastBlockNumber;
		for ( int currentPage = 0; currentPage <= localLastBlockNumber; currentPage++) {
			var block = blockFile.read(currentPage, () -> ByteBuffer.allocate( blockSize ));
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
			var block = blockFile.read(currentPage, () -> ByteBuffer.allocate( blockSize ));
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
		if ( newRecord.size() > blockSize ) {
			throw new IllegalArgumentException("record to large");
		}
		var localLastBlockNumber=lastBlockNumber;
		// iterate over pages
		for ( int currentPage = 0; currentPage <= localLastBlockNumber; currentPage++) {
			var block = blockFile.read(currentPage, () -> ByteBuffer.allocate( blockSize ));
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

		var block = blockFile.read( lastBlockNumber, () -> ByteBuffer.allocate( blockSize ));

		if (block.remaining() < record.size()) {
			block = Block.create( ++lastBlockNumber, blockSize );
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
	 *
	 * @throws IOException
	 */
	public void closeForcibly() throws IOException {
		file.close();
	}

}
