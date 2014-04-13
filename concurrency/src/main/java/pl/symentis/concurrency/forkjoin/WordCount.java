package pl.symentis.concurrency.forkjoin;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class WordCount {

	public static void main(String[] args) throws InterruptedException,
			ExecutionException {

		ForkJoinPool pool = new ForkJoinPool(4);

		ForkJoinTask<Long> submit = pool.submit(new FileReader());

		long time = currentTimeMillis();
		out.println(format("counted words, %d", submit.get()));
		out.println(format("took %s ms", currentTimeMillis() - time));

	}
}
