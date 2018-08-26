package pl.symentis.jit;

public class Sum implements Calculator {
	
	private final int arg1;
	private final int arg2;
	
	public Sum(int arg1, int arg2) {
		super();
		this.arg1 = arg1;
		this.arg2 = arg2;
	}

	@Override
	public int calculate() {
		return arg1+arg2;
	}

}
