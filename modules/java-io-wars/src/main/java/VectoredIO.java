
import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.IntStream;

/**
 * Get it ready with command:
 * 	dd if=/dev/urandom of=geecon.json bs=512 count=1024
 * @author jaroslaw.palka@symentis.pl
 *
 */
public class VectoredIO {

	public static void main(String[] args) throws IOException {
		try (FileChannel file = FileChannel.open(Paths.get("geecon.json"),StandardOpenOption.READ)) {
			
			 ByteBuffer[] dsts = IntStream.range(0, 16)
					 .mapToObj(i -> ByteBuffer.allocate(33554432))
					 .collect(toList())
					 .toArray(new ByteBuffer[16]);

			long readBytes = file.read(dsts);
			out.println(format("file size is = %d", readBytes));
		}
	}

}
