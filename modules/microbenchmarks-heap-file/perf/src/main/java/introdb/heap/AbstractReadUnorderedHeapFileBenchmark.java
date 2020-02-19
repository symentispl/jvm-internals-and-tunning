package introdb.heap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import introdb.api.Entry;
import introdb.api.KeyValueStorage;
import introdb.fs.BlockFile;

@State(Scope.Benchmark)
public abstract class AbstractReadUnorderedHeapFileBenchmark {

	private static final byte[] buffer = new byte[512];

	@Param({ "10", "100", "500" })
	String key;
	KeyValueStorage heapFile;
	Path tempFile;

	@Setup(Level.Trial)
	public void setUp() throws Exception {
		tempFile = Files.createTempFile("heap", "0001");
		heapFile = new UnorderedHeapFile(openBlockFile(tempFile));
		for (int i = 0; i < 1000; i++) {
			heapFile.put(new Entry(Integer.toString(i), buffer));
		}
	}

	abstract BlockFile openBlockFile(Path file) throws IOException;

	@TearDown(Level.Trial)
	public void tearDown() throws Exception {
		Files.delete(tempFile);
	}

	@Benchmark
	public Object readRecord() throws Exception {
		return heapFile.get(key);
	}

}
