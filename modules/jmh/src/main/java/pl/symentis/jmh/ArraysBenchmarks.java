package pl.symentis.jmh;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class ArraysBenchmarks {

	@State(Scope.Benchmark)
	public static class ListOf {
		private List<Long> longs;

		@Setup(Level.Iteration)
		public void setup() {
			longs = LongStream.range(0, 10000).mapToObj(Long::valueOf).collect(Collectors.toList());
		}
	}

	@State(Scope.Benchmark)
	public static class ArrayOf {
		private Long[] longs;

		@Setup(Level.Iteration)
		public void setup() {
			longs = LongStream.range(0, 10000).mapToObj(Long::valueOf).collect(Collectors.toList()).toArray(new Long[]{});
		}
	}

	@Benchmark
	@OperationsPerInvocation(10000)
	public void testList(Blackhole blackhole, ListOf state) {
		for (int i = 0; i < 10000; i++) {
			blackhole.consume(state.longs.get(i));
		}
	}

	@Benchmark
	@OperationsPerInvocation(10000)
	public void testArray(Blackhole blackhole, ArrayOf state) {
		for (int i = 0; i < 10000; i++) {
			blackhole.consume(state.longs[i]);
		}
	}
}
