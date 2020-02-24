package pl.symentis.wordcount;

import org.openjdk.jmh.annotations.*;
import pl.symentis.mapreduce.ForkJoinMapReduce;
import pl.symentis.mapreduce.MapReduce;
import pl.symentis.wordcount.stopwords.Stopwords;

import java.util.HashMap;

@State(Scope.Benchmark)
public class ForkJoinMapReduceWordCountBenchmark {

    @Param({"pl.symentis.wordcount.stopwords.ICUThreadLocalStopwords"})
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
        mapReduce = new ForkJoinMapReduce
                .Builder()
                .build();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        mapReduce.shutdown();
    }

    @Benchmark
    public Object countWords() {
        HashMap<String, Long> map = new HashMap<String, Long>();
        mapReduce.run(
                wordCount.input(ForkJoinMapReduceWordCountBenchmark.class.getResourceAsStream("/big.txt")),
                wordCount.mapReduceJob(),
                map::put);
        return map;
    }

}
