package pl.symentis.concurrency.wordcount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.regex.Pattern;

import pl.symentis.concurrency.mapreduce.Input;
import pl.symentis.concurrency.mapreduce.Mapper;
import pl.symentis.concurrency.mapreduce.Output;
import pl.symentis.concurrency.mapreduce.ParallelWorkflow;
import pl.symentis.concurrency.mapreduce.Reducer;
import pl.symentis.concurrency.mapreduce.SequentialWorkflow;
import pl.symentis.concurrency.mapreduce.Workflow;

public class WordCount {

  static final class WordCountReducer implements Reducer<String, Long> {

    @Override
    public void reduce(String k, Input<Long> input, Output<String, Long> output) {
      Long sum = 0L;
      while (input.hasNext()) {
        sum += input.next();
      }
      output.emit(k, sum);
    }

  }

  static final class WordCountMapper implements Mapper<String, String, Long> {

    private static final Pattern PATTERN = Pattern.compile("\\s|\\p{Punct}");

    private final Collection<CollationKey> stopwords;

    private final ThreadLocal<Collator> threadLocalCollator = new ThreadLocal<>() {

      @Override
      protected Collator initialValue() {
        return (Collator) collator.clone();
      }};
    
    private final Collator collator;

    WordCountMapper(Collator collator, TreeSet<CollationKey> stopwords) {
      this.collator = collator;
      this.stopwords = stopwords;
    }

    @Override
    public void map(String in, Output<String, Long> output) {
      for (String str : PATTERN.split(in.toLowerCase())) {
        if (!stopwords.contains(threadLocalCollator.get().getCollationKey(str))) {
          output.emit(str, 1L);
        }
      }
    }

    private static WordCountMapper from(InputStream input) {
      Collator collator = Collator.getInstance(Locale.ENGLISH);
      TreeSet<CollationKey> stopwords = new TreeSet<>();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
        reader.lines().map(collator::getCollationKey).collect(() -> stopwords, (r, a) -> r.add(a), (r, a) -> r.addAll(a));
      } catch (IOException e) {
        throw new IOError(e);
      }
      return new WordCountMapper(collator,stopwords);
    }

    static WordCountMapper withDefaultStopwords() {
      return from(WordCountMapper.class.getResourceAsStream("stopwords_en.txt"));
    }
  }

  static final class FileLineInput implements Input<String> {

    private final BufferedReader reader;
    private String line;
    private boolean EOF;

    public FileLineInput(File file) throws FileNotFoundException {
      this.reader = new BufferedReader(new FileReader(file));
    }

    public FileLineInput(InputStream input) {
      this.reader = new BufferedReader(new InputStreamReader(input));
    }

    @Override
    public boolean hasNext() {

      if (EOF) {
        return false;
      }

      if (line == null) {
        try {
          line = reader.readLine();
          if (line == null) {
            EOF = true;
            return false;
          } else {
            return true;
          }

        } catch (IOException e) {
          throw new IOError(e);
        }
      }

      return true;
    }

    @Override
    public String next() {
      if (hasNext()) {
        String next = line;
        line = null;
        return next;
      }
      throw new NoSuchElementException();
    }

  }

  public static void main(String[] args) throws Exception {
    Workflow workflow = new SequentialWorkflow();

    Map<String, Long> smap = new HashMap<>();

    long timeMillis = System.currentTimeMillis();
    WordCountMapper wordCountMapper = WordCountMapper.withDefaultStopwords();
    workflow.run(new FileLineInput(new File("src/test/resources/big.txt")), wordCountMapper, new WordCountReducer(), smap::put);
    System.out.println(System.currentTimeMillis() - timeMillis);

    workflow = new ParallelWorkflow();

    Map<String, Long> pmap = new HashMap<>();

    timeMillis = System.currentTimeMillis();
    workflow.run(new FileLineInput(new File("src/test/resources/big.txt")), wordCountMapper, new WordCountReducer(), pmap::put);
    System.out.println(System.currentTimeMillis() - timeMillis);

    workflow.shutdown();

  }

}
