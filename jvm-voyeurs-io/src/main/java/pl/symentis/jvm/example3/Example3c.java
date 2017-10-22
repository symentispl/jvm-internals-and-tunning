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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Example3c {

	private static final Logger LOGGER = LoggerFactory.getLogger(Example3c.class);

	private static final String DEFAULT_SIZE= "128";  // size of file in kB
	private static final String DEFFAULT_CLIENTS = "64"; // number of concurrent client connections
	
	private static final RandomStringGenerator RANDOM_STRING_GEN = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
	
	private final int clients;
	private final int size;
	private final byte[] bytes;
	private final ExecutorService clientConnectionThreadPool;

	public static void main(String[] args) throws IOException, ParseException {
		
		Options options = new Options();
		
		options
		.addOption("c", true,"")
		.addOption("s", true, "");
		
		DefaultParser parser = new DefaultParser();
		CommandLine commandLine = parser.parse(options,args);
		
		int clients = Integer.parseInt(commandLine.getOptionValue("c",DEFFAULT_CLIENTS));
		
		int size = Integer.parseInt(commandLine.getOptionValue("s",DEFAULT_SIZE))*1024;
		
		Example3c example3c = new Example3c(clients,size);
		example3c.run();
		
	}
	
	Example3c(int clients, int size) {
		this.clients = clients;
		this.size = size;
		this.bytes = RANDOM_STRING_GEN.generate(size).getBytes(StandardCharsets.ISO_8859_1);
		this.clientConnectionThreadPool = Executors.newCachedThreadPool();
	}
	
	void run() {
		for(int i=0;i<clients;i++) {
			clientConnectionThreadPool.execute(this::runClientConnection);
		}		
	}

	
	private void runClientConnection() {
		while (true) {
			try {
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
							LOGGER.debug("server {} said give me more", socket);
							continue;
						}

						if ("BYE".equals(line)) {
							LOGGER.debug("server {} said bye", socket);
							break; // close connection, and open new one
						}
					}
				} 
			} catch (IOException e) {
				LOGGER.error("client connect failed",e);
			}
		}
	}

	private void storeFile(OutputStream outputStream, BufferedWriter writer) throws IOException {
		
		String filename = "client_"+RANDOM_STRING_GEN.generate(32);
		
		writer.write(String.format("STORE %s %d",filename,size));
		writer.newLine();
		writer.flush();
		outputStream.write(bytes);
		outputStream.flush();
		writer.newLine();
		writer.flush();
	}

}
