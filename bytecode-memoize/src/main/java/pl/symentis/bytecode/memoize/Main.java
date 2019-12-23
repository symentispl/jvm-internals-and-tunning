package pl.symentis.bytecode.memoize;

public class Main {
	public static void main(String[] args) {
		Calculator calculator = new Calculator();
		
		System.out.println(calculator.doubleInts(1));
		System.out.println(calculator.doubleInts(1));
		System.out.println(calculator.doubleInts(2));
		System.out.println(calculator.doubleInts(2));
		
	}
}
