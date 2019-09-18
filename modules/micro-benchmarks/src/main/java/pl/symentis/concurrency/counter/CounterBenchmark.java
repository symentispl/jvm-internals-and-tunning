package pl.symentis.concurrency.counter;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import pl.symentis.concurrency.primer.counter.LockedCounter;
import pl.symentis.concurrency.primer.counter.ReadWriteLockedCounter;

public class CounterBenchmark {

	@State(Scope.Benchmark)
	public static class CounterState {
		public ReadWriteLockedCounter counter = new ReadWriteLockedCounter();
	}
	
	@Benchmark
	@Group("counter")
	public void increment(final Blackhole bh, final CounterState state) {
		bh.consume(state.counter.inc());
	}
	
	@Benchmark
	@Group("counter")
	public void get(final Blackhole bh, final CounterState state) {
		bh.consume(state.counter.counter());
	}
	
}
