package pl.symentis.concurrency.wordcount;

import java.util.HashMap;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import pl.symentis.concurrency.wordcount.stopwords.Stopwords;
import pl.symentis.mapreduce.MapReduce;
import pl.symentis.mapreduce.MapperOutput;
import pl.symentis.mapreduce.SequentialMapReduce;

@State(Scope.Benchmark)
public class SequentialMapReduceWordCountBenchmark {

	@Param({"pl.symentis.mapreduce.mapper.HashMapOutput"})
	public String mapperOutputClass;

	@Param({"pl.symentis.concurrency.wordcount.stopwords.ICUThreadLocalStopwords"})
	public String stopwordsClass;

	private WordCount wordCount;
	private MapReduce mapReduce;


	@SuppressWarnings("unchecked")
	@Setup(Level.Trial)
	public void setUp() throws Exception {
		wordCount = new WordCount
				.Builder()
				.withStopwords((Class<? extends Stopwords>) Class.forName(stopwordsClass))
				.build();
		mapReduce = new SequentialMapReduce
				.Builder()
				.withMapperOutput((Class<? extends MapperOutput<?, ?>>) Class.forName(mapperOutputClass))
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
				wordCount.input( SequentialMapReduceWordCountBenchmark.class.getResourceAsStream("/big.txt") ),
				wordCount.mapper(),
				wordCount.reducer(), map::put);
		return map;
	}

}
