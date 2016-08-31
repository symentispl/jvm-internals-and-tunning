package pl.symentis.jit;

/**
 * 
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:CompileCommand=print,*NullCheckFolding.nullCheckFolding
 *
 */
public class UncommonTrap {
	static final int CHUNK_SIZE = 1000;

	public static void main(String[] argv) {
		Object trap = null;
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				new Object();
				if (trap != null) {
					System.out.println("I am being trapped!");
					trap = null;
				}
			}
			if (i == 200) {
				trap = new Object();
			}
		}
	}
}
