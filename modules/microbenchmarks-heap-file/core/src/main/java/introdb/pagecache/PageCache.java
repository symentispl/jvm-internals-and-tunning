package introdb.pagecache;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import introdb.fs.Block;

public interface PageCache {

	Page newPage(int blockNymber, Block block);

	Page getPage(int blockNumber, Supplier<ByteBuffer> byteBufferSupplier, BlockLoader loader);

}
