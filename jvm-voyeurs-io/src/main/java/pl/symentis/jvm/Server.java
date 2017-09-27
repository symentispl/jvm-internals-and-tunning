package pl.symentis.jvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

	private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);

	public static void main(String[] args) throws IOException {

		ExecutorService threadPool = Executors.newCachedThreadPool();

		try (ServerSocket serverSocket = new ServerSocket()) {

			serverSocket.bind(new InetSocketAddress("localhost", 7777));
			LOGGER.info("bound to socket {}", serverSocket);

			while (true) {
				Socket socket = serverSocket.accept();
				LOGGER.info("accepted new client connection {}", socket);

				InputStream inputStream = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"ISO-8859-1"));

				OutputStream outputStream = socket.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

				threadPool.execute(newConnectedClient(inputStream, reader, outputStream, writer));

			}
		}

	}

	private static Runnable newConnectedClient(InputStream inputStream, BufferedReader reader,
			OutputStream outputStream, BufferedWriter writer) {
		return () -> {
			try {
				while (true) {
					String line = reader.readLine();
					LOGGER.info("parsing command {}", line);

					Command cmd = parseLine(line);

					char[] buffer = new char[cmd.size];
					LOGGER.info("about to read {} byte(s)", cmd.size);
					IOUtils.readFully(reader, buffer);

					LOGGER.info("writing to a file {}", cmd.filename);
					try (Writer fileWriter = new OutputStreamWriter(Files.newOutputStream(
							Paths.get("/tmp", cmd.filename), StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {

						fileWriter.write(buffer);

					} catch (Throwable e) {
						e.printStackTrace();
					}
					reader.readLine();
					LOGGER.info("waiting for next file");
					writer.write("NEXT");
					writer.newLine();
					writer.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		};
	}

	private static Command parseLine(String line) {
		String[] args = line.split(" ");
		String cmd = args[0];
		String filename = args[1];
		int size = Integer.parseInt(args[2]);
		return new Command(cmd, filename, size);
	}

	static class Command {

		final String cmd;
		final String filename;
		final int size;

		Command(String cmd, String filename, int size) {
			super();
			this.cmd = cmd;
			this.filename = filename;
			this.size = size;
		}
	}
}
