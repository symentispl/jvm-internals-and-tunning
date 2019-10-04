package pl.symentis.concurrency.wordcount;

import java.util.HashMap;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import pl.symentis.concurrency.wordcount.WordCount.WordCountMapper;
import pl.symentis.concurrency.wordcount.WordCount.WordCountReducer;
import pl.symentis.mapreduce.ForkJoinMapReduce;
import pl.symentis.mapreduce.MapReduce;
import pl.symentis.mapreduce.ParallelMapReduce;
import pl.symentis.mapreduce.SequentialMapReduce;

@State(Scope.Benchmark)
public class WordCountBenchmark {

	@Param({ "sequential", "parallel", "forkjoin" })
	public String workflowType;

	private MapReduce workflow;

	private WordCountMapper wordCountMapper;

	private WordCountReducer wordCountReducer;

	@Setup(Level.Trial)
	public void setUp() {
		wordCountMapper = WordCount.WordCountMapper.withDefaultStopwords();
		wordCountReducer = new WordCount.WordCountReducer();
		if ("sequential".equals(workflowType)) {
			workflow = new SequentialMapReduce();
		} else if ("parallel".equals(workflowType)) {
			workflow = new ParallelMapReduce();
		} else if ("forkjoin".equals(workflowType)) {
			workflow = new ForkJoinMapReduce();
		} else {
			throw new IllegalArgumentException();
		}
	}

	@TearDown(Level.Trial)
	public void tearDown() {
		workflow.shutdown();
	}

	@Benchmark
	public Object countWords() throws Exception {
		HashMap<String, Long> map = new HashMap<String, Long>();
		workflow.run(new WordCount.FileLineInput(WordCountBenchmark.class.getResourceAsStream("/big.txt")),
				wordCountMapper, wordCountReducer, map::put);
		return map;
	}

}
