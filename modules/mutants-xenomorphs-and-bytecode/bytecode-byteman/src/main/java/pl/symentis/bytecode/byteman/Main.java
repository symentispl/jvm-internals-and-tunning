package pl.symentis.bytecode.byteman;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple program which creates
 * 
 * @author jarek
 *
 */
public class Main {

	private static final Timer TIMER = new Timer("file processor");

	public static void main(String[] args) {
		TIMER.scheduleAtFixedRate(timerTask(args[0]), 0, 1000);
	}

	private static TimerTask timerTask(String filename) {
		return new TimerTask() {

			@Override
			public void run() {
				FileProcessor fileProcessor = new FileProcessor();
				try {
					List<String> lines = fileProcessor.processFile(filename);
					System.out.println(String.format("file has %s lines", lines.size()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

}
