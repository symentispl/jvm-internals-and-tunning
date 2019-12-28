package introdb.fs;

import static java.lang.String.format;
import static java.lang.System.out;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Debugging utility to assist during corrupted files analysis. It tries as hard
 * as it can to read corrupted heap files and print its content.
 * 
 * @author jaroslaw.palka@symentis.pl
 *
 */
public class HeapFileParser {

	public static void parse(Path path) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(Block.DEFAULT_BLOCK_SIZE);
		int blockNr = 0;
		try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE)) {
			while (true) {
				clear(buffer);
				int bytesRead = fileChannel.read(buffer);
				if(bytesRead==-1) {
					out.println("end of file");
					break;
				}
				buffer.rewind();
				out.println(format("block-number=%d", blockNr++));
				out.println(format("block-marker=%d", buffer.getInt()));
				out.println(format("block-remaining=%d", buffer.getInt()));
				while (true) {
					byte marker = buffer.get();
					out.println(format("\t{"));
					out.println(format("\trecord-marker=%d", marker));
					int keyLength = buffer.getInt();
					out.println(format("\trecord-key-length=%d", keyLength));
					int valueLength = buffer.getInt();
					out.println(format("\trecord-value-length=%d", valueLength));
					out.println(format("\t}"));
					buffer.get(new byte[keyLength]);
					buffer.get(new byte[valueLength]);
					if (marker == 0) {
						break;
					}
				}
			}
		}
	}

	private static void clear(ByteBuffer buffer) {
		buffer.rewind();
		buffer.put(new byte[Block.DEFAULT_BLOCK_SIZE]);
		buffer.rewind();
	}

}
