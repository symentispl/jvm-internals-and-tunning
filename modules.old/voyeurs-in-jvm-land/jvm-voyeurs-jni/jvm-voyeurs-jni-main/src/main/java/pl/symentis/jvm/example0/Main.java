package pl.symentis.jvm.example0;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
	
	public static void main(String[] args) {
		
		ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		
		Process process = new Process();
		scheduledExecutor.scheduleAtFixedRate(() -> process.getPid(), 1000, 1, TimeUnit.MILLISECONDS);
		
	}

}
