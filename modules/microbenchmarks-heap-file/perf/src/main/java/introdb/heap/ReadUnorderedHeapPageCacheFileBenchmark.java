package introdb.heap;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import introdb.fs.BlockFile;
import introdb.fs.FileChannelBlockFile;
import introdb.pagecache.HashMapPageCache;
import introdb.pagecache.PageCacheBlockFile;

public class ReadUnorderedHeapPageCacheFileBenchmark extends AbstractReadUnorderedHeapFileBenchmark {

	BlockFile openBlockFile(Path file) throws IOException {
		FileChannelBlockFile blockFile = new FileChannelBlockFile(FileChannel.open(file, READ, WRITE), 4 * 1024);
		return new PageCacheBlockFile(new HashMapPageCache(blockFile, 1024, 0.2f), blockFile);
	}

}
