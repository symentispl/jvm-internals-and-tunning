package introdb.heap;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import introdb.fs.BlockFile;
import introdb.fs.FileChannelBlockFile;

public class ReadUnorderedHeapFileBenchmark extends AbstractReadUnorderedHeapFileBenchmark {

	@Override
	BlockFile openBlockFile(Path file) throws IOException {
		FileChannel fileChannel = FileChannel.open(file, READ, WRITE);
		return new FileChannelBlockFile(fileChannel, 4 * 1024);
	}

}
