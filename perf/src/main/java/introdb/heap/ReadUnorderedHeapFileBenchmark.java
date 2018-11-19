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
public class ReadUnorderedHeapFileBenchmark {
	
	private static final byte[] buffer = new byte[512];
	
	@Param({"10","100","500"})
	public String key;
	
	private Store heapFile;
	private Path tempFile;
	
	@Setup(Level.Trial)
	public void setUp() throws Exception {
		tempFile = Files.createTempFile("heap", "0001");
		heapFile = new UnorderedHeapFile(tempFile, 50000, 4*1024);
		for(int i=0;i<1000;i++) {
			heapFile.put(new Entry(Integer.toString(i),buffer));			
		}
	}
	
	@TearDown(Level.Trial)
	public void tearDown() throws Exception{
		Files.delete(tempFile);
	}
	
    @Benchmark
    public Object readKey() throws Exception {
    	return heapFile.get(key);
    }

}
