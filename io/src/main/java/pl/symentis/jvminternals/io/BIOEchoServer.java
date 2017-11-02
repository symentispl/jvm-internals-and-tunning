package pl.symentis.jvminternals.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BIOEchoServer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BIOEchoServer.class);

	public static void main(String[] args) throws IOException,
			InterruptedException {

		try (ServerSocket serverSocket = new ServerSocket(8888)) {

			while (true) {
				final Socket socket = serverSocket.accept();

				LOGGER.debug("new connection");

				Runnable connection = new Runnable() {

					@Override
					public void run() {
						try {
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(
											socket.getInputStream()));
							String line = reader.readLine();
							PrintWriter writer = new PrintWriter(
									socket.getOutputStream());
							writer.print(line);
							writer.flush();

						} catch (IOException e) {
							LOGGER.error("", e);
						} finally {
							try {
								socket.close();
							} catch (IOException e) {
								LOGGER.warn("", e);
							}
						}
					}
				};

				Thread serverThread = new Thread(connection);
				serverThread.start();

				serverThread.join();
			}
		}
	}
}
