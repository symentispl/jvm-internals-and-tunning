import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MMap {

	private static final int _32G_SIZE = 32 * 1024 * 1024 * 1024;

	public static void main(String[] args) throws IOException {
		MappedByteBuffer byteBuffer = FileChannel
				.open(Paths.get("large-file.bin"), StandardOpenOption.READ)
				.map(MapMode.READ_ONLY, 0, _32G_SIZE);

		byte[] dst = new byte[1024];
		while(byteBuffer.hasRemaining()) {
			byteBuffer.get(dst);
		}
		
	}

}
