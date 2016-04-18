package pl.symentis.jit;

public class NullCheckFolding {

	static final int CHUNK_SIZE = 1000;

	public static void assertNotNull(Object obj) {
		if (obj == null) {
			System.out.println(String.format("%s is null", obj));
		}
	}

	public void nullCheckFodling() {
		assertNotNull(this);
	}
	
	public static void main(String[] args) {
		NullCheckFolding ncf = new NullCheckFolding();
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				ncf.nullCheckFodling();
			}
		}
	}

}
