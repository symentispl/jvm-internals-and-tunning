package pl.symentis.jvm.concurrency.forkjoin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;

public class LineReader extends RecursiveTask<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Reader reader;
	
	public LineReader(Reader reader) {
		super();
		this.reader = reader;
	}



	@Override
	protected Long compute() {

		ArrayList<CountWords> counters = new ArrayList<>();
		
		try (BufferedReader buffered = new BufferedReader(reader);) {

			String line;
			while ((line = buffered.readLine()) != null) {
				CountWords c = new CountWords(line);
				counters.add(c);
				c.fork();
			}

			long z = 0;

			for (CountWords w : counters) {
				z = z + w.join();
			}

			return z;

		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}