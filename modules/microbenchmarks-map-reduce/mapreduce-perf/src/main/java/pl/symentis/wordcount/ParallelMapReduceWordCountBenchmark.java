package pl.symentis.wordcount;

import org.openjdk.jmh.annotations.*;
import pl.symentis.mapreduce.MapReduce;
import pl.symentis.mapreduce.ParallelMapReduce;
import pl.symentis.wordcount.stopwords.Stopwords;

import java.util.HashMap;

@State(Scope.Benchmark)
public class ParallelMapReduceWordCountBenchmark {

    @Param({"pl.symentis.wordcount.stopwords.ICUThreadLocalStopwords"})
    public String stopwordsClass;

    @Param({"8"})
    public int threadPoolMaxSize;

    @Param({"1000"})
    public int phaserMaxTasks;

    private WordCount wordCount;
    private MapReduce mapReduce;

    @SuppressWarnings("unchecked")
    @Setup(Level.Trial)
    public void setUp() throws Exception {
        wordCount = new WordCount
                .Builder()
                .withStopwords((Class<? extends Stopwords>) Class.forName(stopwordsClass))
                .build();
        mapReduce = new ParallelMapReduce
                .Builder()
                .withPhaserMaxTasks(phaserMaxTasks)
                .withThreadPoolSize(threadPoolMaxSize)
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
                wordCount.input(ParallelMapReduceWordCountBenchmark.class.getResourceAsStream("/big.txt")),
                wordCount.mapper(),
                wordCount.reducer(),
                map::put);
        return map;
    }

}
