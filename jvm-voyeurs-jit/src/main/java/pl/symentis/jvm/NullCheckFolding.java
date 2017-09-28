package pl.symentis.jvm;

/**
 * 
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:CompileCommand=print,*NullCheckFolding.nullCheckFolding
 *
 */
public class NullCheckFolding {

	static final int CHUNK_SIZE = 1000;

	public static void assertNotNull(Object obj) {
		if (obj == null) {
			System.out.println(String.format("%s is null", obj));
		}
	}

	public void nullCheckFolding() {
		assertNotNull(this);
	}
	
	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		NullCheckFolding ncf = new NullCheckFolding();
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				ncf.nullCheckFolding();
			}
		}
		System.out.println(System.currentTimeMillis()-time);
	}

}
