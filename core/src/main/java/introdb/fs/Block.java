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

	byte[] array();

	int position();

	int remaining();

	void flush(BlockFile bf) throws IOException;

	Optional<PersistentRecord> write(TransientRecord record);

	RecordCursor cursor();

}
