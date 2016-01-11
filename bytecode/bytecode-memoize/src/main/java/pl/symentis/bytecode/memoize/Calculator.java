package pl.symentis.bytecode.memoize;

public class Calculator {

	@Memoize
	public int doubleInts(int i) {
		return i + i;
	}

}
