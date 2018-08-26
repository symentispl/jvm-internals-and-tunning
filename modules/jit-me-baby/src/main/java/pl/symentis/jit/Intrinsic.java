package pl.symentis.jit;

/**
 *
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:+PrintInlining -XX:CompileCommand=print,*Intrinsic.intrinsic
 *
 */
public class Intrinsic {
	static final int CHUNK_SIZE = 1000;

	private static long[] intrinsic(long[] arr){
	    long[] destArr = new long[arr.length];
	    System.arraycopy(arr, 0, destArr, 0, arr.length);
	    return destArr;
	}

	public static void main(String[] argv) {
		long[] arr = new long[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				arr = intrinsic(arr);
			}
		}
	}
}
