package pl.symentis.jvm.examplex;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {

	private static final int TRANSFER_SIZE = 128*1024*1024;

	public static void main(String[] args) throws IOException {
		transferFromZeroToHero();
		
	}

	private static void transferFromZeroToHero() throws IOException {
		FileChannel in = FileChannel.open(Paths.get("/dev/urandom"), StandardOpenOption.READ);
		FileChannel out = FileChannel.open(Paths.get("/dev/null"), StandardOpenOption.WRITE);
		
		while(true) {
			in.transferTo(0, TRANSFER_SIZE, out);
		}
	}

}
