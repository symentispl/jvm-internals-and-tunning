package introdb.heap;

import java.nio.file.Files;
import java.nio.file.Path;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class WriteUnorderedHeapFileBenchmark {
		
	@Param( {"512","1024","2048"})
	public int bufferSize; 
	private byte[] buffer;
	private Store heapFile;
	private int key;
	private Path tempFile;
	
	@Setup(Level.Iteration)
	public void setUp() throws Exception {
		tempFile = Files.createTempFile("heap", "0001");
		heapFile = new UnorderedHeapFile(tempFile, 50000, 4*1024);
		buffer = new byte[bufferSize];
		key = 0;
	}
	
	@TearDown(Level.Iteration)
	public void tearDown() throws Exception{
		Files.delete(tempFile);
	}
	
    @Benchmark
    public void writeBuffer() throws Exception {
    	heapFile.put(new Entry(key++,buffer));
    }

}
