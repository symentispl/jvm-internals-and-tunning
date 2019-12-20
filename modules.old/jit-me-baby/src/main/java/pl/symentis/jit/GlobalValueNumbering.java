package pl.symentis.jit;

/**
 * 
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:CompileCommand=print,*GlobalValueNumbering.globalValueNumbering
 *
 */
public class GlobalValueNumbering {

	static final int CHUNK_SIZE = 1000;
	
	public static long globalValueNumbering(int i) {
		  int x = i+4;
		  return 2*x;
	}
	
	public static void main(String[] args) {
		long result = 0;
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				result = globalValueNumbering(j);
			}
		}
		System.out.println(result);
	}

}
