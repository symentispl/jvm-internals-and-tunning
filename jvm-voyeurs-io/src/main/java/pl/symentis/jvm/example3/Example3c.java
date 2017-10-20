package pl.symentis.jvm.example3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Example3c {

	private final static Logger LOGGER = LoggerFactory.getLogger(Example3c.class);
	
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

					if ("NEXT".equals(line)) {
						LOGGER.info("server {} said give me more",socket);
						continue;
					}

					if ("BYE".equals(line)) {
						LOGGER.info("server {} said bye",socket);
						break;
					}
				}

			}
		}
	}

	private static void storeFile(OutputStream outputStream, BufferedWriter writer) throws IOException {
		
		int size = RandomUtils.nextInt(16*1024, 32*1024);
		String filename = "client_"+new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(32);
		
		writer.write(String.format("STORE %s %d",filename,size));
		writer.newLine();
		writer.flush();
		outputStream.write(RandomUtils.nextBytes(size));
		outputStream.flush();
		writer.newLine();
		writer.flush();
	}

}
