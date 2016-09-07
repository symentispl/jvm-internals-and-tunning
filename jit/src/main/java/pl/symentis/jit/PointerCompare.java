package pl.symentis.jit;

import java.util.ArrayList;

/**
 * 
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:CompileCommand=print,*PointerCompare.pointerCompare
 *
 */
public class PointerCompare {

	static final int CHUNK_SIZE = 1000;

	public static int pointerCompare(Object obj) {
		Object anotherObj = new Object();
		if(obj == anotherObj){
			return 0;
		}
		return -1;
	}
	
	public static void doInLoop(){
		new ArrayList<>().add("");
	}

	public static void main(String[] args) {
		int result = 0;
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				result = pointerCompare(new Object());
			}
		}
		System.out.println(result);
	}

}
