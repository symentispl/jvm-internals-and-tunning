package introdb.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import introdb.record.PersistentRecord;
import introdb.record.TransientRecord;

public interface Block extends Iterable<PersistentRecord> {

	public static Block byteBufferBlock(int blockNr, ByteBuffer byteBuffer) {
		return new ByteBufferBlock(blockNr, byteBuffer);
	}

	/**
	 * Return byte array which represents block content
	 * 
	 * @return
	 */
	byte[] array();

	/**
	 * Current write position in block
	 * 
	 * @return
	 */
	int position();

	/**
	 * Number of remaining free bytes in block
	 * 
	 * @return
	 */
	int remaining();

	/**
	 * Write record to a next free slot
	 * 
	 * @param record
	 * @return
	 */
	Optional<PersistentRecord> write(TransientRecord record);

	/**
	 * Flush block to underlying storage
	 * 
	 * @throws IOException
	 */
	void flush(BlockFile blockFile) throws IOException;

	RecordCursor cursor();

}
