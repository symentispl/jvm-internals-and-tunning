package introdb.heap;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import introdb.fs.BlockFile;
import introdb.fs.FileChannelBlockFile;

public class ReadUnorderedHeapFileBenchmark extends AbstractReadUnorderedHeapFileBenchmark {

	@Override
	BlockFile openBlockFile(Path file) throws IOException {
		return new FileChannelBlockFile(
				FileChannel.open(
						file, 
						StandardOpenOption.READ, 
						StandardOpenOption.WRITE),
				4 * 1024);
	}

}
