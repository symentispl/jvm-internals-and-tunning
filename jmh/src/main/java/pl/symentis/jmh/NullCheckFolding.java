package pl.symentis.jmh;

public class NullCheckFolding {

	public String fold() {
		nullCheck(this);
		return "done";
	}

	public static void nullCheck(NullCheckFolding nullCheckFolding) {
		if (nullCheckFolding == null) {
			System.out.println("assert null check folding");
		}
	}

}
