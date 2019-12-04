package pl.symentis.concurrency.mapreduce;

import org.junit.jupiter.api.Test;
import pl.symentis.mapreduce.BatchingParallelMapReduce;
import pl.symentis.mapreduce.ForkJoinMapReduce;
import pl.symentis.mapreduce.MapReduce;
import pl.symentis.mapreduce.SequentialMapReduce;
import pl.symentis.wordcount.WordCount;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchingParallelJoinMapReduceTest {

    @Test
    public void batchingParallelMapReduce() throws Exception {

        WordCount wordCount = new WordCount.Builder().build();

        MapReduce workflow = new SequentialMapReduce.Builder().build();
        Map<String, Long> smap = new HashMap<>();
        workflow.run(
                wordCount.input(new File("src/test/resources/big.txt")),
                wordCount.mapper(),
                wordCount.reducer(),
                smap::put);
        workflow.shutdown();

        workflow = new BatchingParallelMapReduce.Builder().build();
        Map<String, Long> fmap = new HashMap<>();
        workflow.run(
                wordCount.input(new File("src/test/resources/big.txt")),
                wordCount.mapper(),
                wordCount.reducer(),
                fmap::put);
        workflow.shutdown();

        assertThat(fmap).isEqualTo(smap);
    }

    @Test
    public void mergeResults(){
        Map<String, List<String>> map0 = Map.of("one", List.of("0"));
        Map<String, List<String>> map1 = Map.of("one", List.of("1"));

        ConcurrentLinkedDeque<Map<String, List<String>>> dequeue = new ConcurrentLinkedDeque<>();
        dequeue.offer(map0);
        dequeue.offer(map1);

        Map<String, List<String>> map = BatchingParallelMapReduce.merge(dequeue);
        System.out.println(map);
    }

}
