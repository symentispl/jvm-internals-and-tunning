package introdb.heap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class WriteUnorderedHeapFileBenchmark {
	
	private static final byte[] smallBuffer = new byte[512];
	private static final byte[] mediumBuffer = new byte[1024];
	private static final byte[] biggerBuffer = new byte[2048];
	
	private Store heapFile;
	private int key;
	private Path tempFile;
	
	@Setup(Level.Iteration)
	public void setUp() throws IOException {
		tempFile = Files.createTempFile("heap", "0001");
		heapFile = new UnorderedHeapFile(tempFile, 50000, 4*1024);
		key = 0;
	}
	
	@TearDown(Level.Iteration)
	public void tearDown() throws IOException{
		Files.delete(tempFile);
	}
	
    @Benchmark
    public void writeSmallBuffer() throws ClassNotFoundException, IOException {
    	heapFile.put(new Entry(key++,smallBuffer));
    }

    @Benchmark
    public void writeMediumBuffer() throws ClassNotFoundException, IOException {
    	heapFile.put(new Entry(key++,mediumBuffer));
    }

    @Benchmark
    public void writeBiggerBuffer() throws ClassNotFoundException, IOException {
    	heapFile.put(new Entry(key++,biggerBuffer));
    }
}
