package pl.symentis.bytecode.memoize;

public class Calculator {

	@Memoize
	public Integer doubleInts(Integer i) {
		System.out.println(String.format("doubleInts(%d)",i));
		return i + i;
	}

}
