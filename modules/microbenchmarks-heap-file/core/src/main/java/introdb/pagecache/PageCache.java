package introdb.pagecache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import introdb.fs.Block;

public interface PageCache {

	/**
	 * Creates page for block
	 * 
	 * @param blockNymber
	 * @param block
	 * @return
	 */
	Page newPage(int blockNymber, Block block);

	/**
	 * Gets page from cache (if page is not loaded load it)
	 * 
	 * @param blockNumber
	 * @param byteBufferSupplier
	 * @param loader
	 * @return
	 * @throws IOException
	 */
	Page getPage(int blockNumber, Supplier<ByteBuffer> byteBufferSupplier, BlockLoader loader) throws IOException;

}
