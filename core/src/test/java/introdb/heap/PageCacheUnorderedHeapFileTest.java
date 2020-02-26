package introdb.heap;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import introdb.api.KeyValueStorage;
import introdb.fs.FileChannelBlockFile;
import introdb.pagecache.HashMapPageCache;
import introdb.pagecache.PageCacheBlockFile;

class PageCacheUnorderedHeapFileTest implements UnorderedHeapFileTest{

	Path heapFilePath;
	KeyValueStorage heapFile;

	@BeforeEach
	public void setUp() throws IOException {
		heapFilePath = Files.createTempFile("heap", "0001");
		FileChannel fileChannel = FileChannel.open(heapFilePath, StandardOpenOption.READ, StandardOpenOption.WRITE);
		FileChannelBlockFile fileChannelBlockFile = new FileChannelBlockFile(fileChannel, 4 * 1024);
		PageCacheBlockFile blockFile = new PageCacheBlockFile(new HashMapPageCache(fileChannelBlockFile, 1024, 0.2f), fileChannelBlockFile);
		heapFile = new UnorderedHeapFile(blockFile);
	}

	@AfterEach
	public void tearDown() throws IOException {
		Files.delete(heapFilePath);
	}

	@Override
	public KeyValueStorage heapFile() {
		return heapFile;
	}
	

}
