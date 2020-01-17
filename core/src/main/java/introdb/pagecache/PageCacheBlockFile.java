package introdb.pagecache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import introdb.fs.Block;
import introdb.fs.BlockFile;

public class PageCacheBlockFile implements BlockFile {

	private final PageCache pageCache;
	private final BlockFile blockFile;

	public PageCacheBlockFile(PageCache pageCache, BlockFile blockFile) {
		super();
		this.pageCache = pageCache;
		this.blockFile = blockFile;
	}

	@Override
	public void write(int blockNumber, ByteBuffer byteBuffer) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Block read(int blockNumber, Supplier<ByteBuffer> byteBufferSupplier) throws IOException {
		Page page = pageCache.getPage(blockNumber, byteBufferSupplier, blockFile::read);
		return new PageBlock(page);
	}

	@Override
	public Block create(int blockNumber, int blockSize) {
		Block block = blockFile.create(blockNumber, blockSize);
		Page page = pageCache.newPage(blockNumber, block);
		return new PageBlock(page);
	}

	@Override
	public void close() throws Exception {
		blockFile.close();
	}

	@Override
	public int blockSize() {
		return blockFile.blockSize();
	}

}
