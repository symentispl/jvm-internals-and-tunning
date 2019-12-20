package pl.symentis.jit;

public class UncommonTrapCHA {
	static final int CHUNK_SIZE = 1000;

	public static void main(String[] argv) {
		Calculator trap = new Sum(1, 1);
		int result = 0;
		for (int i = 0; i < 250; ++i) {
			for (int j = 0; j < CHUNK_SIZE; ++j) {
				result = trap.calculate();
			}
			if (i == 200) {
				System.out.println("I am being trapped!");
				trap = new Multiply(1, 1);
			}
		}
		System.out.println(result);
	}
}
