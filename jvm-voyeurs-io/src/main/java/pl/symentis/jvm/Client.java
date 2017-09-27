package pl.symentis.jvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;

public class Client {

	public static void main(String[] args) throws IOException {

		while (true) {
			try (Socket socket = new Socket()) {
				socket.connect(new InetSocketAddress("localhost", 7777));

				OutputStream outputStream = socket.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

				InputStream inputStream = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

				while (true) {
					storeFile(outputStream, writer);

					String line = reader.readLine();

					System.out.println("line: " + line);

					if ("NEXT".equals(line)) {
						continue;
					}

					if ("BYE".equals(line)) {
						break;
					}
				}

			}
		}
	}

	private static void storeFile(OutputStream outputStream, BufferedWriter writer) throws IOException {
		
		int size = RandomUtils.nextInt(16*1024, 32*1024);
		String filename = new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(32);
		
		writer.write(String.format("STORE %s %d",filename,size));
		writer.newLine();
		writer.flush();
		outputStream.write(RandomUtils.nextBytes(size));
		outputStream.flush();
		writer.newLine();
		writer.flush();
	}

}
