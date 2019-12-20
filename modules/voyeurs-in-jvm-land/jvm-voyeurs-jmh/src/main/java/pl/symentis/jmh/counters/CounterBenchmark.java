package pl.symentis.jmh.counters;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class CounterBenchmark {

	
	private Counter counter = new Counter();	
	
	@Benchmark
	public void incCounter() {
		counter.inc();
	}
	
}
