package pl.symentis.concurrency.forkjoin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;

public class FileReader extends RecursiveTask<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected Long compute() {

		ArrayList<CountWords> counters = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new java.io.FileReader(
				"/home/jarek/bigger.txt"));) {

			String line;
			while ((line = reader.readLine()) != null) {

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