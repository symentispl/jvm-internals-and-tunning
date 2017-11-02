package pl.symentis.jvminternals.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NIOEchoServer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NIOEchoServer.class);

	public static void main(String[] args) throws IOException {

		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", 9999));

		Selector selector = Selector.open();

		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (selector.select() > 0) {

			Iterator<SelectionKey> iterator = selector.selectedKeys()
					.iterator();
			while (iterator.hasNext()) {
				SelectionKey selectionKey = iterator.next();

				if (selectionKey.isAcceptable()) {
					
					System.out.println("new connection");
					
					ServerSocketChannel ssc = (ServerSocketChannel) selectionKey
							.channel();
					SocketChannel socketChannel = ssc.accept();
					socketChannel.configureBlocking(false);
					socketChannel.register(selector, SelectionKey.OP_READ);
					continue;
				}
				if (selectionKey.isReadable()) {
					System.out.println("reading");
					SocketChannel socketChannel = (SocketChannel) selectionKey
							.channel();

					ByteBuffer dst = ByteBuffer.allocate(1024);
					socketChannel.read(dst);
					dst.flip();
					Charset charset = Charset.forName("UTF-8");
					CharsetDecoder decoder = charset.newDecoder();
					CharBuffer cbuf = decoder.decode(dst);
					System.out.print(cbuf.toString());

					socketChannel.register(selector, SelectionKey.OP_WRITE);
					continue;
				}
			}

		}

	}
}
