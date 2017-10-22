package pl.symentis.jvm.example3;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Example3s {

	private final static Logger LOGGER = LoggerFactory.getLogger(Example3s.class);

	private static final int MAX_FILES = 10;

	private static ExecutorService janitorExecutor;

	private static ExecutorService clientConnectionsPool;

	public static void main(String[] args) throws IOException {

		janitorExecutor = Executors.newSingleThreadExecutor();
		
		clientConnectionsPool = Executors.newCachedThreadPool();

		try (ServerSocket serverSocket = new ServerSocket()) {

			serverSocket.bind(new InetSocketAddress("localhost", 7777));
			LOGGER.debug("bound to socket {}", serverSocket);

			while (true) {
				Socket socket = serverSocket.accept();
				LOGGER.debug("accepted new client connection {}", socket);

				InputStream inputStream = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"));

				OutputStream outputStream = socket.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

				clientConnectionsPool.execute(newConnectedClient(inputStream, reader, outputStream, writer));

			}
		}

	}

	private static Runnable newConnectedClient(
			InputStream inputStream, 
			BufferedReader reader,
			OutputStream outputStream, 
			BufferedWriter writer) {
		return () -> {
			try {
				int i = 1;
				List<String> storedFiles = new ArrayList<>();
				while (true) {
					String line = reader.readLine();
					LOGGER.debug("parsing command {}", line);

					Command cmd = parseLine(line);

					char[] buffer = new char[cmd.size];
					LOGGER.debug("about to read {} byte(s)", cmd.size);
					IOUtils.readFully(reader, buffer);

					LOGGER.debug("writing to a file {}", cmd.filename);
					try (Writer fileWriter = new OutputStreamWriter(newFileOutput(cmd.filename))) {

						fileWriter.write(buffer);

					} catch (Throwable e) {
						LOGGER.error("failed to write file {}",cmd.filename,e);
					} finally {
						storedFiles.add(cmd.filename);
					}
					
					reader.readLine();

					if ((i % MAX_FILES)!=0) {
						LOGGER.debug("waiting for next file");
						writer.write("NEXT");
						writer.newLine();
						writer.flush();
					} else {
						LOGGER.debug("enough files, bye");
						writer.write("BYE");
						writer.newLine();
						writer.flush();
						i=0;
						scheduleJanitor(new ArrayList<>(storedFiles));
						storedFiles.clear();
						break;
					}
					i++;
				}
			} catch (IOException e) {
				LOGGER.error("I/O is not that simple", e);
			}

		};
	}

	private static void scheduleJanitor(List<String> filenames) {
		janitorExecutor.execute(() -> 
			filenames
			.stream()
			.map(Example3s::newPath)
			.forEach(Example3s::deleteIfExists));
	}

	private static void deleteIfExists(Path f) {
		try {
			Files.deleteIfExists(f);
		} catch (IOException e) {
			LOGGER.error("failed to remove file {}",f,e);
		}
	}

	private static Path newPath(String filename) {
		return Paths.get("/tmp", filename);
	}

	private static OutputStream newFileOutput(String filename) throws IOException {
		return Files.newOutputStream(newPath(filename), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
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
