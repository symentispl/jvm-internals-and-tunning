package pl.symentis.mmap;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MMapMain {

	private static final Stopwatch STOPWATCH = SimonManager.getStopwatch("Journal.fold");
	private static final Logger LOGGER = LoggerFactory.getLogger(MMapMain.class);
	
	public static void main(String[] args) throws IOException {
		
		ScheduledExecutorService monitoringExecutor = Executors.newSingleThreadScheduledExecutor();
		
		monitoringExecutor.scheduleAtFixedRate(() -> {
			LOGGER.info("accumulated journal query time is {}", STOPWATCH);								
		}, 1, 10, TimeUnit.SECONDS);

		Journal journal = new Journal(Files.createTempDirectory("example4"));

		ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);

		for (int i = 0; i < 16; i++) {
			scheduledThreadPool.scheduleWithFixedDelay(() -> {
				try {
					Split split = STOPWATCH.start();
					long sumOfAll = journal.fold(MMapMain::sum);
					split.stop();
					LOGGER.debug("current value is {}",sumOfAll);
				} catch (IOException e) {
					LOGGER.error("failed to query journal",e);
				}
			}, 10, 1, TimeUnit.MILLISECONDS);
		}

		int i = 0;
		while (true) {
			journal.append(new Record(i++));
		}
	}
	
	private static long sum(long l, Record rec) {
		return l + rec.getValue();
	}

}
