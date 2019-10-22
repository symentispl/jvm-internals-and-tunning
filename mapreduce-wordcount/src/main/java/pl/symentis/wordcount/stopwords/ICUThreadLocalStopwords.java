package pl.symentis.wordcount.stopwords;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.TreeSet;

import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

public class ICUThreadLocalStopwords implements Stopwords {

	private final ThreadLocal<Collator> threadLocalCollator = new ThreadLocal<Collator>() {

		@Override
		protected Collator initialValue() {
			try {
				return (Collator) collator.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
	};

	private Collator collator;
	private final TreeSet<CollationKey> stopwords;

	public static Stopwords from(InputStream inputStream) {
		Collator collator = Collator.getInstance(Locale.ENGLISH);
		TreeSet<CollationKey> stopwords = new TreeSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			reader.lines().map(collator::getCollationKey).collect(() -> stopwords, TreeSet::add, TreeSet::addAll);
		} catch (IOException e) {
			throw new IOError(e);
		}
		return new ICUThreadLocalStopwords(collator, stopwords);
	}

	private ICUThreadLocalStopwords(Collator collator, TreeSet<CollationKey> stopwords) {
		this.collator = collator;
		this.stopwords = stopwords;
	}

	@Override
	public boolean contains(String str) {
		return stopwords.contains(threadLocalCollator.get().getCollationKey(str));
	}

}