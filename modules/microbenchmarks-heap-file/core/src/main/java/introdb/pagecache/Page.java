package introdb.pagecache;

import introdb.fs.Block;

class Page {

	private final Block block;
	private boolean dirty;

	public Page(Block block) {
		super();
		this.block = block;
	}

	Block block() {
		return block;
	}

	boolean dirty() {
		return dirty;
	}

	public void markDirty() {
		dirty = true;
	}

}
