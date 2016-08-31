package pl.symentis.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class NullCheckFoldingBenchmark {

	@State(Scope.Benchmark)
	public static class BenchmarkState {
		private final NullCheckFolding nullCheck = new NullCheckFolding();
	}

	@Benchmark
	public void testMethod(Blackhole blackhole, BenchmarkState state) {
		blackhole.consume(state.nullCheck.fold());
	}

}
