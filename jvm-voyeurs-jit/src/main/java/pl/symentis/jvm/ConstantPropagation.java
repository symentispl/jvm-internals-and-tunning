package pl.symentis.jvm;

/**
 * 
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:CompileCommand=print,*ConstantPropagation.constantPropagation
 *
 */
public class ConstantPropagation {

	static final int CHUNK_SIZE = 1000;
	
	public static long constantPropagation() {
		  int x = 14;
		  int y = 7 - x / 2;
		  return y * (28 / x + 2);
	}
	
	public static void main(String[] args) {
		long result = 0;
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				result = constantPropagation();
			}
		}
		System.out.println(result);
	}

}
