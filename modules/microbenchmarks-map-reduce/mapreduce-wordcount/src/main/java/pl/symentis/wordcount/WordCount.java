package pl.symentis.wordcount;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import pl.symentis.mapreduce.Input;
import pl.symentis.mapreduce.MapReduceJob;
import pl.symentis.mapreduce.Mapper;
import pl.symentis.mapreduce.Output;
import pl.symentis.mapreduce.Reducer;
import pl.symentis.wordcount.stopwords.ICUThreadLocalStopwords;
import pl.symentis.wordcount.stopwords.Stopwords;

public class WordCount {

    public static class Builder {

        private Class<? extends Stopwords> stopwordsClass = ICUThreadLocalStopwords.class;

        public Builder withStopwords(Class<? extends Stopwords> stopwordsClass) {
            this.stopwordsClass = stopwordsClass;
            return this;
        }

        public WordCount build() {
            try {
                Stopwords stopwords = (Stopwords) stopwordsClass
                        .getMethod("from", InputStream.class)
                        .invoke(stopwordsClass, WordCount.class.getResourceAsStream("stopwords_en.txt"));
                return new WordCount(stopwords);
            } catch (IllegalAccessException |
                    IllegalArgumentException |
                    InvocationTargetException |
                    NoSuchMethodException |
                    SecurityException e) {
                throw new RuntimeException(format("cannot instantiate stopwords %s", stopwordsClass), e);
            }
        }
    }

    private final Stopwords stopwords;

    public WordCount(Stopwords stopwords) {
        this.stopwords = stopwords;
    }

    public Input<String> input(File file) throws FileNotFoundException {
        return new FileLineInput(file);
    }

    public Input<String> input(InputStream inputStream) {
        return new FileLineInput(inputStream);
    }

    public Mapper<String, String, Long> mapper() {
        return new WordCountMapper(stopwords);
    }

    public Reducer<String, Long, Long> reducer() {
        return new WordCountReducer();
    }
    
    public MapReduceJob<String, String, Long, Long> mapReduceJob(){
    	return new MapReduceJob<String, String, Long, Long>(mapper(), reducer(), () -> 0L, (a,b) -> a+b);
    }
    
    public MapReduceJob<String, String, Long, Long> tokenizerMapReduceJob(){
    	return new MapReduceJob<String, String, Long, Long>(new WordCountTokenizerMapper(stopwords), reducer(), () -> 0L, (a,b) -> a+b);
    }

    static final class WordCountReducer implements Reducer<String, Long, Long> {

        @Override
        public Long reduce(String k, Iterable<Long> input) {
            Long sum = 0L;
            for (Long l : input) {
                sum += l;
            }
            return sum;
        }

    }

    static final class WordCountMapper implements Mapper<String, String, Long> {

        private static final Pattern PATTERN = Pattern.compile("\\s|\\p{Punct}");

        private final Stopwords stopwords;

        WordCountMapper(Stopwords stopwords) {
            this.stopwords = stopwords;
        }

        @Override
        public void map(String in, Output<String, Long> output) {
            for (String str : PATTERN.split(in.toLowerCase())) {
                if (!stopwords.contains(str)) {
                    output.emit(str, 1L);
                }
            }
        }
    }

    static final class WordCountTokenizerMapper implements Mapper<String, String, Long> {

        private final Stopwords stopwords;

        WordCountTokenizerMapper(Stopwords stopwords) {
            this.stopwords = stopwords;
        }

        @Override
        public void map(String in, Output<String, Long> output) {
        	StringTokenizer stringTokenizer = new StringTokenizer(in.toLowerCase()," \t\n\r!'#%&'()*+,-./:;<=>?@[/]^_{|}~");
            while(stringTokenizer.hasMoreTokens()) {
            	String str = stringTokenizer.nextToken();
                if (!stopwords.contains(str)) {
                    output.emit(str, 1L);
                }
            }
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

}
