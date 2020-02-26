package introdb.pagecache;

import introdb.fs.Block;

class Page {

	private final int blockNumber;
	private final Block block;
	private final long createTimestamp = System.currentTimeMillis();
	private boolean dirty;

	public Page(int blockNumber, Block block) {
		super();
		this.blockNumber = blockNumber;
		this.block = block;
	}

	Block block() {
		return block;
	}
	
	int blockNumber() {
		return blockNumber;
	}

	boolean dirty() {
		return dirty;
	}

	public void markDirty() {
		dirty = true;
	}

	long createTimestamp() {
		return createTimestamp;
	}
	
}
