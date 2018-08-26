package pl.symentis.jit;

import java.util.ArrayList;

/**
 * 
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:CompileCommand=print,*LockEllision.lockEllision
 *
 */
public class LockEllision {
    
    static final Object lock = new Object();

	static final int CHUNK_SIZE = 1000;

	public static int lockEllision(int j) {
	    Object obj = new Object();
	    synchronized (obj) {
         j++;
	    }	    
	    return j;
	}
	
	public static void doInLoop(){
		new ArrayList<>().add("");
	}

	public static void main(String[] args) {
		int result = 0;
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				result = lockEllision(j);
			}
		}
		System.out.println(result);
	}

}
