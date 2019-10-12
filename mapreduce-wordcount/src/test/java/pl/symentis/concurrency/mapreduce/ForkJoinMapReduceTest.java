package pl.symentis.concurrency.mapreduce;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import pl.symentis.concurrency.wordcount.WordCount;
import pl.symentis.mapreduce.ForkJoinMapReduce;
import pl.symentis.mapreduce.MapReduce;
import pl.symentis.mapreduce.Mapper;
import pl.symentis.mapreduce.SequentialMapReduce;

public class ForkJoinMapReduceTest {

	@Test
	public void forkJoinMapReduce() throws FileNotFoundException {
	    MapReduce workflow = new SequentialMapReduce.Builder().build();
	    Map<String, Long> smap = new HashMap<>();
	    Mapper<String, String, Long> wordCountMapper = WordCount.mapperWithDefaultStopwords();
	    workflow.run(
	    		WordCount.input(new File("src/test/resources/big.txt")), 
	    		wordCountMapper,
	    		WordCount.reducer(),
	    		smap::put);
	    workflow.shutdown();

	    workflow = new ForkJoinMapReduce();
	    Map<String, Long> fmap = new HashMap<>();
	    workflow.run(
	    		WordCount.input(new File("src/test/resources/big.txt")),
	    		wordCountMapper,
	    		WordCount.reducer(),
	    		fmap::put);
	    workflow.shutdown();

	    assertThat(fmap).isEqualTo(smap);
	}
	
}
