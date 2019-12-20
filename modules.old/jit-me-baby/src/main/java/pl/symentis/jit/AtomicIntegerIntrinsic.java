package pl.symentis.jit;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:CompileCommand=print,*AtomicIntegerIntrinsic.incInteger
 *
 */
public class AtomicIntegerIntrinsic {

	static final int CHUNK_SIZE = 1000;

	static final AtomicInteger atomicInt = new AtomicInteger();
	
	public static long incInteger() {
		return atomicInt.incrementAndGet();
	}
	
	public static void doInLoop(){
		new ArrayList<>().add("");
	}

	public static void main(String[] args) {
		long result = 0;
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				result = incInteger();
			}
		}
		System.out.println(result);
	}

}
