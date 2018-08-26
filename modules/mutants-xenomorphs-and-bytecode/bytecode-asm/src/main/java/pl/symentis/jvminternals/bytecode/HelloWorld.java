package pl.symentis.jvminternals.bytecode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class HelloWorld {
	
	@SafeVarargs
	public static void main(String... args) throws FileNotFoundException, IOException {
		try(InputStream is = new FileInputStream("/tmp")){
			is.read();
		}
	}

}
