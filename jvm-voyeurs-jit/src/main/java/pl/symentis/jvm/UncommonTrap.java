package pl.symentis.jvm;

/**
 *
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:+PrintInlining
 *
 */
public class UncommonTrap {
	static final int CHUNK_SIZE = 1000;

	private static Object uncommonTrap(Object trap){
		if (trap != null) {
			System.out.println("I am being trapped!");
		}
		return null;
	}

	public static void main(String[] argv) {
		Object trap = null;
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				trap = uncommonTrap(trap);
			}
			if (i == 200) {
				trap = new Object();
			}
		}
	}
}
