package pl.symentis.wordcount;

import java.util.HashMap;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import pl.symentis.mapreduce.BatchingParallelMapReduce;
import pl.symentis.mapreduce.MapReduce;
import pl.symentis.wordcount.WordCount;
import pl.symentis.wordcount.stopwords.Stopwords;

@State(Scope.Benchmark)
public class BatchingParallelMapReduceWordCountBenchmark {

	@Param({"pl.symentis.wordcount.stopwords.ICUThreadLocalStopwords"})
	public String stopwordsClass;
	
	@Param({"8"})
	public int threadPoolMaxSize;
	
	@Param({"1000"})
	public int phaserMaxTasks;
	
	@Param({"1000"})
	public int batchSize;
	
	private WordCount wordCount;
	private MapReduce mapReduce;

	@SuppressWarnings("unchecked")
	@Setup(Level.Trial)
	public void setUp() throws Exception {
		wordCount = new WordCount
				.Builder()
				.withStopwords((Class<? extends Stopwords>) Class.forName(stopwordsClass))
				.build();
		mapReduce = new BatchingParallelMapReduce
				.Builder()
				.withPhaserMaxTasks(phaserMaxTasks)
				.withThreadPoolSize(threadPoolMaxSize)
				.withBatchSize(batchSize)
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
				wordCount.input(BatchingParallelMapReduceWordCountBenchmark.class.getResourceAsStream("/text8")),
				wordCount.mapper(),
				wordCount.reducer(),
				map::put);
		return map;
	}

}
