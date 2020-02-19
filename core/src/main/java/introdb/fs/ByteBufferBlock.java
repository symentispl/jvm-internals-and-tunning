package introdb.fs;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Logger;

import introdb.record.PersistentRecord;
import introdb.record.Record;
import introdb.record.Record.Mark;
import introdb.record.TransientRecord;

/**
 * Represents physical block on disc.
 * 
 * Each block starts with header: <br>
 * 
 * <ul>
 * <li>block start marker - 4 bytes, should be equal to {@code Integer.MAX_VALUE}, this helps us to distinguish new block (marker will be equal to 0)</li>
 * <li>remaining space in block - 4 bytes</li>
 * </ul>
 * 
 * 
 * @author jaroslaw.palka@symentis.pl
 *
 */
class ByteBufferBlock implements Block {
	
	private final static Logger LOGGER = Logger.getLogger(ByteBufferBlock.class.getName());

	static final int DEFAULT_BLOCK_SIZE = 4 * 1024;
	static final int BLOCK_HEADER_SIZE = Integer.BYTES // marker, when new block equal to 0, otherwise Integer.MAX
										 +Integer.BYTES;

	/**
	 * Creates new block
	 * 
	 * @return
	 */
	static Block create(int blockNr, int blockSize) {
		ByteBuffer buffer = ByteBuffer.allocate(blockSize);
		// write block header
		buffer.putInt(Integer.MAX_VALUE);
		buffer.putInt(blockSize - BLOCK_HEADER_SIZE);
		return new ByteBufferBlock(blockNr, buffer);
	}

	/**
	 * Reads block from {@code ByteBuffer}
	 * 
	 * @param blockNr
	 * @param blockSize
	 * @param buffer
	 * @return
	 */
	static Block read(int blockNr, int blockSize, ByteBuffer buffer) {
		if (buffer.remaining() < blockSize) {
			throw new IllegalStateException("buffer has not enough bytes for block");
		}

		// read block header
		int marker = buffer.getInt();
		if(marker==0) {
			// new block, initialize marker
			buffer.putInt(0, Integer.MAX_VALUE);
			buffer.putInt(blockSize-BLOCK_HEADER_SIZE);
			return new ByteBufferBlock(blockNr, buffer);
		} else if(marker==Integer.MAX_VALUE) {
			int remaining = buffer.getInt();
			if (remaining < 0 || remaining > blockSize) {
				throw new IllegalStateException(format("invalid block header, remaining bytes field %d", remaining));
			}
			buffer.position(buffer.capacity() - remaining);			
			return new ByteBufferBlock(blockNr, buffer);
		} else {
			throw new IllegalStateException("block corrupted, marker is not 0 or Integer.MAX_VALUE");
		}
	}

	private final int blockNr;
	private final ByteBuffer buffer;

	public ByteBufferBlock(int blockNr, ByteBuffer buffer) {
		super();
		this.blockNr = blockNr;
		this.buffer = buffer;
	}


	@Override
	public RecordCursor cursor() {
		return new BlockRecordCursor(buffer.asReadOnlyBuffer().position(BLOCK_HEADER_SIZE));
	}

	@Override
	public Optional<PersistentRecord> write(TransientRecord record) {
		if (record.size() <= buffer.remaining()) {
			// mark record position
			int offset = buffer.position();
			record.write(buffer);
			// update block header
			buffer.putInt(/*TODO this is fragile can change when structure of block header changes*/4, buffer.remaining());
			return Optional
					.of(new PersistentRecord(buffer, Record.Mark.PRESENT, offset, record.key().length, record.value().length));
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	public void flush(BlockFile bf) throws IOException {
		bf.write(blockNr, buffer.asReadOnlyBuffer().rewind());
	}

	public class BlockRecordCursor implements RecordCursor {

		private final ByteBuffer roBuffer;
		private boolean hasNext;
		private PersistentRecord record;

		public BlockRecordCursor(ByteBuffer roBuffer) {
			this.roBuffer = roBuffer;
		}

		@Override
		public boolean hasNext() {
			if (!hasNext) {
				roBuffer.mark();
				record = PersistentRecord.read(roBuffer);
				if (record.isEmpty()) {
					// thanks to this trick we will not have buffer overflow
					roBuffer.reset();
					return false;
				}
				hasNext=true;
			}
			return hasNext;
		}

		@Override
		public PersistentRecord next() {
			if (hasNext()) {
				hasNext = false;
				return record;
			} else {
				throw new NoSuchElementException();
			}
		}
		

		@Override
		public void remove() {
			LOGGER.fine( ()->String.format("removing record %s", record));
			buffer.put(record.offset(), (byte)Mark.REMOVED.mark());
		}

		@Override
		public int position() {
			return roBuffer.position();
		}

		@Override
		public void close() throws Exception {
			
		}

	}

	@Override
	public int remaining() {
		return buffer.remaining();
	}

	@Override
	public int position() {
		return buffer.position();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Block [blockNr=").append(blockNr)
			   .append(", buffer=").append(buffer)
			   .append("]");
		return builder.toString();
	}

	@Override
	public byte[] array() {
		return buffer.array();
	}

	@Override
	public Iterator<PersistentRecord> iterator() {
		return cursor();
	}
	
}
