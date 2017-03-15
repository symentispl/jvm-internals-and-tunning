package pl.symentis.bytecode.instrument;

import java.util.Arrays;

public class TestClass implements TestInterface{

	@Override
	@TestAnnotation
	public Integer testMethod(Integer i) {
		System.out.println("testMethod "+i);
		return i+i;
	}
	
	public static void main(String[] args) {
		System.out.println(Arrays.asList(new Integer(1)).equals(Arrays.asList(new Integer(1))));
	}

}
