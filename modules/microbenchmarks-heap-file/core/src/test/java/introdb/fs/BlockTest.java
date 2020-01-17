package introdb.fs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import introdb.api.Entry;
import introdb.fs.ByteBufferBlock;
import introdb.fs.ByteBufferBlock.BLockRecordCursor;
import introdb.record.PersistentRecord;
import introdb.record.Record;
import introdb.record.TransientRecord;

public class BlockTest {

	@Test
	public void emptyBlockIteratorisEmpty() {
		var block = ByteBufferBlock.create(0, ByteBufferBlock.DEFAULT_BLOCK_SIZE);
		var cursor = block.cursor();
		assertThat(cursor.hasNext()).isFalse();
		assertThat(cursor.hasNext()).isFalse();
	}

	@Test
	public void emptyBlockIteratorThrowsNoSuchElementOnNext() {
		var block = ByteBufferBlock.create(0, ByteBufferBlock.DEFAULT_BLOCK_SIZE);
		var cursor = block.cursor();
		assertThatThrownBy(() -> cursor.next()).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	public void emptyBlockIteratorThrowsNoSuchElementOnNextAfterHasNext() {
		var block = ByteBufferBlock.create(0, ByteBufferBlock.DEFAULT_BLOCK_SIZE);
		var cursor = block.cursor();
		cursor.hasNext();
		assertThatThrownBy(() -> cursor.next()).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	public void blockCursorSamePositionWhenHasNextFalse() {
		var block = ByteBufferBlock.create(0, ByteBufferBlock.DEFAULT_BLOCK_SIZE);
		var cursor = block.cursor();
		var position = cursor.position();
		assertThat(cursor.hasNext()).isFalse();
		assertThat(cursor.position()).isEqualTo(position);
	}

	@Test
	public void readBlockFromFullBuffer() {
		var buffer = ByteBuffer.allocate(ByteBufferBlock.DEFAULT_BLOCK_SIZE)
									  .putInt(Integer.MAX_VALUE)
									  .putInt(0)
									  .rewind();
		
		var block = ByteBufferBlock.read(0, ByteBufferBlock.DEFAULT_BLOCK_SIZE, buffer);
		assertThat(block.remaining()).isEqualTo(0);
		var cursor = block.cursor();
		assertThat(cursor.hasNext()).isFalse();
	}

	@Test
	public void readBlockFromEmptyBuffer() {
		// given
		ByteBuffer buffer = ByteBuffer.allocate(ByteBufferBlock.DEFAULT_BLOCK_SIZE)
									  .putInt(Integer.MAX_VALUE)
									  .putInt(ByteBufferBlock.DEFAULT_BLOCK_SIZE - ByteBufferBlock.BLOCK_HEADER_SIZE)
									  .rewind();

		// when
		Block block = ByteBufferBlock.read(0, ByteBufferBlock.DEFAULT_BLOCK_SIZE, buffer);

		// than
		assertThat(block.remaining()).isEqualTo(ByteBufferBlock.DEFAULT_BLOCK_SIZE - ByteBufferBlock.BLOCK_HEADER_SIZE);
		assertThat(block.position()).isEqualTo(ByteBufferBlock.BLOCK_HEADER_SIZE);

		// when
		RecordCursor cursor = block.cursor();

		// then
		assertThat(cursor.hasNext()).isFalse();
	}

	@Test
	public void writeRecordToBlock() throws IOException {
		// given
		ByteBuffer buffer = ByteBuffer.allocate(ByteBufferBlock.DEFAULT_BLOCK_SIZE)
									  .putInt(Integer.MAX_VALUE)
							          .putInt(ByteBufferBlock.DEFAULT_BLOCK_SIZE - ByteBufferBlock.BLOCK_HEADER_SIZE)
							          .rewind();
		Block block = ByteBufferBlock.read(0, ByteBufferBlock.DEFAULT_BLOCK_SIZE, buffer);
		TransientRecord transientRecord0 = TransientRecord.of(new Entry("1", "1"));

		// when
		Optional<PersistentRecord> record0 = block.write(transientRecord0);

		// then
		assertThat(record0).get().satisfies(persistenRecord -> {
			assertThat(persistenRecord.mark()).isEqualTo(Record.Mark.PRESENT);
			assertThat(persistenRecord.offset()).isEqualTo(ByteBufferBlock.BLOCK_HEADER_SIZE);
		});
		assertThat(block.remaining())
				.isEqualTo(ByteBufferBlock.DEFAULT_BLOCK_SIZE - ByteBufferBlock.BLOCK_HEADER_SIZE - transientRecord0.size());

		// given
		TransientRecord transientRecord1 = TransientRecord.of(new Entry("2", "2"));

		// when
		Optional<PersistentRecord> record1 = block.write(transientRecord1);
		assertThat(record1).get().satisfies(persistenRecord -> {
			assertThat(persistenRecord.mark()).isEqualTo(Record.Mark.PRESENT);
			assertThat(persistenRecord.offset()).isEqualTo(ByteBufferBlock.BLOCK_HEADER_SIZE + transientRecord0.size());
		});
		assertThat(block.remaining()).isEqualTo(ByteBufferBlock.DEFAULT_BLOCK_SIZE - ByteBufferBlock.BLOCK_HEADER_SIZE
				- transientRecord0.size() - transientRecord1.size());

	}

	@Test
	public void dontwriteRecordToBlockWhenNoSpaceRemaining() throws IOException {
		// given
		ByteBuffer buffer = ByteBuffer.allocate(ByteBufferBlock.DEFAULT_BLOCK_SIZE)
				                      .putInt(Integer.MAX_VALUE)
				                      .putInt(ByteBufferBlock.DEFAULT_BLOCK_SIZE - ByteBufferBlock.BLOCK_HEADER_SIZE)
				                      .rewind();
		Block block = ByteBufferBlock.read(0, ByteBufferBlock.DEFAULT_BLOCK_SIZE, buffer);
		TransientRecord transientRecord = TransientRecord.of(new Entry("1", new byte[ByteBufferBlock.DEFAULT_BLOCK_SIZE]));
		
		// when
		Optional<PersistentRecord> record = block.write(transientRecord);
		
		// then
		assertThat(record).isEmpty();
	}
}
