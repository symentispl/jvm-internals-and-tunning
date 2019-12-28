package introdb.heap;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import introdb.api.Entry;

@State(Scope.Benchmark)
public class WriteUnorderedHeapFileBenchmark {

	@Param({ "512", "1024", "2048" })
	public int bufferSize;
	private byte[] buffer;
	private int key;
	private List<UnorderedHeapFile> copies;

	@Setup(Level.Iteration)
	public void setUp() throws Exception {
		buffer = new byte[bufferSize];
		key = 0;

		copies = IntStream.range(0, 1000)
				.mapToObj(i -> {
					try {
						return new UnorderedHeapFile(Files.createTempFile("heap.", "." + i), 50000, 4 * 1024);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				})
				.collect(toList());
	}

	@TearDown(Level.Iteration)
	public void tearDown() throws IOException {
		for(UnorderedHeapFile heapFile:copies) {
			heapFile.closeForcibly();
		}
	}

	@Benchmark
	@OperationsPerInvocation(1000)
	public void writeBuffer() throws Exception {
		for(UnorderedHeapFile heapFile:copies) {
			heapFile.put(new Entry(key++, buffer));			
		}
	}

}
