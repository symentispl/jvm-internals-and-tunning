package introdb.pagecache;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import introdb.fs.Block;
import introdb.fs.BlockFile;
import introdb.fs.RecordCursor;
import introdb.record.PersistentRecord;
import introdb.record.TransientRecord;

class PageBlock implements Block {

	private final Page page;

	PageBlock(Page page) {
		super();
		this.page = page;
	}

	@Override
	public byte[] array() {
		return page.block().array();
	}

	@Override
	public int position() {
		return page.block().position();
	}

	@Override
	public int remaining() {
		return page.block().remaining();
	}

	@Override
	public void flush(BlockFile bf) throws IOException {
		// no-op, page cache should handle it
	}

	@Override
	public Optional<PersistentRecord> write(TransientRecord record) {
		page.markDirty();
		return page.block().write(record);
	}

	@Override
	public RecordCursor cursor() {
		return page.block().cursor();
	}

	@Override
	public Iterator<PersistentRecord> iterator() {
		return page.block().iterator();
	}

}
