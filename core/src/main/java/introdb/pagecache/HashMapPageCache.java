package introdb.pagecache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.function.Supplier;

import introdb.fs.Block;
import introdb.fs.BlockFile;

public class HashMapPageCache implements PageCache {

	private final BlockFile blockFile;
	private final HashMap<Integer, Page> pageCache;

	private final int maxSize;
	private final float maxDirtyRatio;
	
	public HashMapPageCache(BlockFile blockFile, int maxSize, float maxDirtyRatio) {
		this.blockFile = blockFile;
		this.maxSize = maxSize;
		this.pageCache = new HashMap<>(maxSize);
		this.maxDirtyRatio = maxDirtyRatio;
	}

	@Override
	public Page getPage(int blockNumber, Supplier<ByteBuffer> byteBufferSupplier, BlockLoader loader) throws IOException {
		if (dirtyRatio() > maxDirtyRatio || pageCache.size()>maxSize) {
			invalidate();
		}
		return pageCache.computeIfAbsent(blockNumber, key -> {
			try {
				return new Page(blockNumber, loader.read(key, byteBufferSupplier));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public Page newPage(int blockNumber, Block block) {
		return pageCache.computeIfAbsent(blockNumber, key -> {
			return new Page(blockNumber,block);
		});
	}
	
	public int maxSize() {
		return maxSize;
	}

	private float dirtyRatio() {
		if(pageCache.size()==0) {
			return 0;
		}
		return pageCache.values().stream().filter(Page::dirty).count() / (float) pageCache.size();
	}

	private void invalidate() throws IOException {
		Page oldest = pageCache.values()
			.stream()
			.sorted( (p1,p2) -> Long.compare(p1.createTimestamp(), p2.createTimestamp()))
			.findFirst()
			.get();
		pageCache.remove(oldest.blockNumber());
		if(oldest.dirty()) {
			oldest.block().flush(blockFile);
		}
	}

}
