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
import pl.symentis.mapreduce.SequentialMapReduce;

public class ForkJoinMapReduceTest {

	@Test
	public void forkJoinMapReduce() throws FileNotFoundException {

		WordCount wordCount = new WordCount.Builder().build();
		
		MapReduce workflow = new SequentialMapReduce.Builder().build();
	    Map<String, Long> smap = new HashMap<>();
	    workflow.run(
	    		wordCount.input(new File("src/test/resources/big.txt")), 
	    		wordCount.mapper(),
	    		wordCount.reducer(),
	    		smap::put);
	    workflow.shutdown();

	    workflow = new ForkJoinMapReduce();
	    Map<String, Long> fmap = new HashMap<>();
	    workflow.run(
	    		wordCount.input(new File("src/test/resources/big.txt")),
	    		wordCount.mapper(),
	    		wordCount.reducer(),
	    		fmap::put);
	    workflow.shutdown();

	    assertThat(fmap).isEqualTo(smap);
	}
	
}
