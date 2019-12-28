package pl.symentis.wordcount;

import org.openjdk.jmh.annotations.*;
import pl.symentis.mapreduce.MapReduce;
import pl.symentis.mapreduce.MapperOutput;
import pl.symentis.mapreduce.SequentialMapReduce;
import pl.symentis.wordcount.stopwords.Stopwords;

import java.util.HashMap;

@State(Scope.Benchmark)
public class SequentialMapReduceWordCountBenchmark {

    @Param({"pl.symentis.mapreduce.mapper.HashMapOutput"})
    public String mapperOutputClass;

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
                wordCount.input(SequentialMapReduceWordCountBenchmark.class.getResourceAsStream("/big.txt")),
                wordCount.mapper(),
                wordCount.reducer(), map::put);
        return map;
    }

}
