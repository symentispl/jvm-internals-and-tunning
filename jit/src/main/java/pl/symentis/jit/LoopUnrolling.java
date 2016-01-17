package pl.symentis.jit;

public class LoopUnrolling {

	static final int CHUNK_SIZE = 1000;

	public static int loopUnrolling() {
		int j = 0;
		for (int i = 0; i < 10; i++) {
			j++;
		}
		return j;
	}

	public static void main(String[] args) {
		int result = 0;
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				result = loopUnrolling();
			}
		}
		System.out.println(result);
	}

}
