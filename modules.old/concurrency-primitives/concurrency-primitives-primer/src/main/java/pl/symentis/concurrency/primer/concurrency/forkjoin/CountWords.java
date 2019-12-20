package pl.symentis.concurrency.primer.concurrency.forkjoin;

import static java.lang.Long.valueOf;

import java.util.concurrent.RecursiveTask;

public class CountWords extends RecursiveTask<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String line;

	public CountWords(String line) {
		super();
		this.line = line;
	}

	@Override
	protected Long compute() {
		return valueOf(line.split("[\\s\\p{Punct}]").length);
	}

}