package pl.symentis.jit;

import static java.lang.String.format;
import static java.lang.System.out;

/**
 * case #0
 * 		-Xint
 * case #1
 * 		-XX:+UseCompiler -XX:MaxRecursiveInlineLevel=1
 * case #2
 * 		-XX:+UseCompiler -XX:MaxRecursiveInlineLevel=2
 * 
 * @author jaroslaw.palka@symentis.pl
 *
 */
public class Fibonacci {
	
	static final int CHUNK_SIZE = 1;

    public static long fibonacci(int n) {
        if (n <= 1) return n;
        else return fibonacci(n-1) + fibonacci(n-2);
    }

    public static void main(String[] args) {
		long result = 0;
		long time = System.currentTimeMillis();
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				result = fibonacci(24);
			}
		}
		out.println(format("execute time is %d ms", System.currentTimeMillis()-time));
		out.println(format("result is %d ", result));
    }

}