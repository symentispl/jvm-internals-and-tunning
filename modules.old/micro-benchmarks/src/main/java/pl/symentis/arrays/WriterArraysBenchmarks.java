package pl.symentis.arrays;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class WriterArraysBenchmarks {

	@State(Scope.Benchmark)
	public static class ListOf {

		@Param( {"16","10000"} )
		public int size;
		
		private List<Long> longs;

		@Setup(Level.Iteration)
		public void setup() {
			longs = new ArrayList<Long>(size);
		}
	}

	@State(Scope.Benchmark)
	public static class LinkedListOf {
		private List<Long> longs;

		@Setup(Level.Iteration)
		public void setup() {
			longs = new LinkedList<Long>();
		}
	}

	@State(Scope.Benchmark)
	public static class ArrayOf {
		private Long[] longs;

		@Setup(Level.Iteration)
		public void setup() {
			longs = new Long[10000];
		}
	}

	@Benchmark
	@OperationsPerInvocation(10000)
	public void testList(ListOf state) {
		for (int i = 0; i < 10000; i++) {
			state.longs.add(Long.valueOf(i));
		}
	}

	@Benchmark
	@OperationsPerInvocation(10000)
	public void testArray(Blackhole blackhole, ArrayOf state) {
		for (int i = 0; i < 10000; i++) {
			state.longs[i] = Long.valueOf(i);
		}
	}
	@Benchmark
	@OperationsPerInvocation(10000)
	public void testLinkedList(Blackhole blackhole, LinkedListOf state) {
		for (int i = 0; i < 10000; i++) {
			state.longs.add(Long.valueOf(i));
		}
	}
	
}
