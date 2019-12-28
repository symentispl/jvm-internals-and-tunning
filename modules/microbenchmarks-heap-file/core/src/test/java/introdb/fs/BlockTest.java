package introdb.fs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import introdb.api.Entry;
import introdb.fs.Block;
import introdb.fs.Block.Cursor;
import introdb.record.PersistentRecord;
import introdb.record.Record;
import introdb.record.TransientRecord;

public class BlockTest {

	@Test
	public void emptyBlockIteratorisEmpty() {
		var block = Block.create(0, Block.DEFAULT_BLOCK_SIZE);
		var iterator = block.iterator();
		assertThat(iterator.hasNext()).isFalse();
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void emptyBlockIteratorThrowsNoSuchElementOnNext() {
		var block = Block.create(0, Block.DEFAULT_BLOCK_SIZE);
		var iterator = block.iterator();
		assertThatThrownBy(() -> iterator.next()).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	public void emptyBlockIteratorThrowsNoSuchElementOnNextAfterHasNext() {
		var block = Block.create(0, Block.DEFAULT_BLOCK_SIZE);
		var iterator = block.iterator();

		iterator.hasNext();
		assertThatThrownBy(() -> iterator.next()).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	public void blockCursorSamePositionWhenHasNextFalse() {
		var block = Block.create(0, Block.DEFAULT_BLOCK_SIZE);
		var cursor = block.cursor();
		var position = cursor.position();
		assertThat(cursor.hasNext()).isFalse();
		assertThat(cursor.position()).isEqualTo(position);
	}

	@Test
	public void readBlockFromFullBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(Block.DEFAULT_BLOCK_SIZE)
									  .putInt(Integer.MAX_VALUE)
									  .putInt(0)
									  .rewind();
		
		Block block = Block.read(0, Block.DEFAULT_BLOCK_SIZE, buffer);
		assertThat(block.remaining()).isEqualTo(0);
		Cursor cursor = block.cursor();
		assertThat(cursor.hasNext()).isFalse();
	}

	@Test
	public void readBlockFromEmptyBuffer() {
		// given
		ByteBuffer buffer = ByteBuffer.allocate(Block.DEFAULT_BLOCK_SIZE)
									  .putInt(Integer.MAX_VALUE)
									  .putInt(Block.DEFAULT_BLOCK_SIZE - Block.BLOCK_HEADER_SIZE)
									  .rewind();

		// when
		Block block = Block.read(0, Block.DEFAULT_BLOCK_SIZE, buffer);

		// than
		assertThat(block.remaining()).isEqualTo(Block.DEFAULT_BLOCK_SIZE - Block.BLOCK_HEADER_SIZE);
		assertThat(block.position()).isEqualTo(Block.BLOCK_HEADER_SIZE);

		// when
		Cursor cursor = block.cursor();

		// then
		assertThat(cursor.hasNext()).isFalse();
	}

	@Test
	public void writeRecordToBlock() throws IOException {
		// given
		ByteBuffer buffer = ByteBuffer.allocate(Block.DEFAULT_BLOCK_SIZE)
									  .putInt(Integer.MAX_VALUE)
							          .putInt(Block.DEFAULT_BLOCK_SIZE - Block.BLOCK_HEADER_SIZE)
							          .rewind();
		Block block = Block.read(0, Block.DEFAULT_BLOCK_SIZE, buffer);
		TransientRecord transientRecord0 = TransientRecord.of(new Entry("1", "1"));

		// when
		Optional<PersistentRecord> record0 = block.write(transientRecord0);

		// then
		assertThat(record0).get().satisfies(persistenRecord -> {
			assertThat(persistenRecord.mark()).isEqualTo(Record.Mark.PRESENT);
			assertThat(persistenRecord.offset()).isEqualTo(Block.BLOCK_HEADER_SIZE);
		});
		assertThat(block.remaining())
				.isEqualTo(Block.DEFAULT_BLOCK_SIZE - Block.BLOCK_HEADER_SIZE - transientRecord0.size());

		// given
		TransientRecord transientRecord1 = TransientRecord.of(new Entry("2", "2"));

		// when
		Optional<PersistentRecord> record1 = block.write(transientRecord1);
		assertThat(record1).get().satisfies(persistenRecord -> {
			assertThat(persistenRecord.mark()).isEqualTo(Record.Mark.PRESENT);
			assertThat(persistenRecord.offset()).isEqualTo(Block.BLOCK_HEADER_SIZE + transientRecord0.size());
		});
		assertThat(block.remaining()).isEqualTo(Block.DEFAULT_BLOCK_SIZE - Block.BLOCK_HEADER_SIZE
				- transientRecord0.size() - transientRecord1.size());

	}

	@Test
	public void dontwriteRecordToBlockWhenNoSpaceRemaining() throws IOException {
		// given
		ByteBuffer buffer = ByteBuffer.allocate(Block.DEFAULT_BLOCK_SIZE)
				                      .putInt(Integer.MAX_VALUE)
				                      .putInt(Block.DEFAULT_BLOCK_SIZE - Block.BLOCK_HEADER_SIZE)
				                      .rewind();
		Block block = Block.read(0, Block.DEFAULT_BLOCK_SIZE, buffer);
		TransientRecord transientRecord = TransientRecord.of(new Entry("1", new byte[Block.DEFAULT_BLOCK_SIZE]));
		
		// when
		Optional<PersistentRecord> record = block.write(transientRecord);
		
		// then
		assertThat(record).isEmpty();
	}
}
