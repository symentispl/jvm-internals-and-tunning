package pl.symentis.concurrency.wordcount;

import java.util.HashMap;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import pl.symentis.concurrency.wordcount.WordCount.WordCountMapper;
import pl.symentis.concurrency.wordcount.WordCount.WordCountReducer;
import pl.symentis.mapreduce.ForkJoinMapReduce;
import pl.symentis.mapreduce.MapReduce;

@State(Scope.Benchmark)
public class ForkJoinMapReduceWordCountBenchmark {

	private MapReduce mapReduce;

	private WordCountMapper wordCountMapper;

	private WordCountReducer wordCountReducer;

	@Setup(Level.Trial)
	public void setUp() throws Exception {
		wordCountMapper = WordCount.WordCountMapper.withDefaultStopwords();
		wordCountReducer = new WordCount.WordCountReducer();
		mapReduce = new ForkJoinMapReduce
				.Builder()
				.build();
	}

	@TearDown(Level.Trial)
	public void tearDown() {
		mapReduce.shutdown();
	}

	@Benchmark
	public Object countWords() throws Exception {
		HashMap<String, Long> map = new HashMap<String, Long>();
		mapReduce.run(
				new WordCount.FileLineInput(
						ParallelMapReduceWordCountBenchmark.class.getResourceAsStream("/big.txt")),
				wordCountMapper, wordCountReducer, map::put);
		return map;
	}

}
