package pl.symentis.jvminternals.gc;

import java.util.Random;
import java.util.WeakHashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

/**
 * Hello world!
 * 
 */
public class App {
	public static void main(String[] args) {

		WeakHashMap<String, String> map = new WeakHashMap<String, String>();
		Stopwatch stopwatch = SimonManager.getStopwatch("gc-bench");
		Random random = new Random();
		while (true) {
			Split split = stopwatch.start();
			int i = 0;
			while (i < 100000) {
				String key = RandomStringUtils.random(random.nextInt(16384)+1);
				String value = key+key;
				map.put(key, value);
				i++;
			}
			split.stop();System.out.println(stopwatch);
		}
	}
}
