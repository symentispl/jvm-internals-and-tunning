package introdb.pagecache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.function.Supplier;

import introdb.fs.Block;

public class HashMapPageCache implements PageCache {

	private final HashMap<Integer, Page> pageCache = new HashMap<>();

	@Override
	public Page getPage(int blockNumber, Supplier<ByteBuffer> byteBufferSupplier, BlockLoader loader) {
		return pageCache.computeIfAbsent(blockNumber, key -> {
			try {
				return new Page(loader.read(key, byteBufferSupplier));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public Page newPage(int blockNymber, Block block) {
		return pageCache.computeIfAbsent(blockNymber, key -> {
			return new Page(block);
		});
	}

}
