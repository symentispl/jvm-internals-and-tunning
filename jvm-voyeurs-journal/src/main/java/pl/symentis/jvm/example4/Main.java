package pl.symentis.jvm.example4;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {

	public static void main(String[] args) throws IOException {
		Journal journal = new Journal(Paths.get("/tmp"));
		int i = 0;
		while (true) {
			journal.append(new Record(i++));
			System.out.println(journal.fold((v, r) -> v + r.getValue()));
		}
	}

}
