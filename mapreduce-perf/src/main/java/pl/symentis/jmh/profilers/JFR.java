package pl.symentis.jmh.profilers;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.ExternalProfiler;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.Aggregator;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ResultRole;
import org.openjdk.jmh.runner.IterationType;

public final class JFR implements ExternalProfiler, InternalProfiler {

	private volatile String filename;
	private volatile boolean afterWarmup;

	@Override
	public Collection<String> addJVMInvokeOptions(final BenchmarkParams params) {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> addJVMOptions(final BenchmarkParams params) {
		return Arrays.asList("-XX:+FlightRecorder");
	}

	@Override
	public void beforeTrial(final BenchmarkParams benchmarkParams) {
	}

	@Override
	public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {

		if(!afterWarmup && iterationParams.getType() == IterationType.MEASUREMENT) {
			afterWarmup = true;
			Path jcmd = Paths.get(benchmarkParams.getJvm()).getParent().resolve("jcmd");
			filename = format("%s.jfr", benchmarkParams.id());
			try {
				int exitCode = new ProcessBuilder()
					.command( 
							jcmd.toString(), 
							Long.toString(ProcessHandle.current().pid()),
							"JFR.start",
							format("filename=%s dumponexit=true disk=true settings=profile", filename))
					.start()
					.waitFor();
				if(exitCode!=0) {
					throw new RuntimeException("failed to run jcmd command");
				}
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}

	@Override
	public Collection<? extends Result> afterIteration(
			BenchmarkParams benchmarkParams,
			IterationParams iterationParams,
			IterationResult result) {
		NoResult r = new NoResult("Profile saved to " + filename);
		return Collections.singleton(r);
	}

	@Override
	public Collection<? extends Result> afterTrial(final BenchmarkResult bp, final long l, final File file,
			final File file1) {
		
		System.out.println(bp.getBenchmarkResults());
		
		return Collections.emptyList();
	}

	@Override
	public boolean allowPrintOut() {
		return false;
	}

	@Override
	public boolean allowPrintErr() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Java Flight Recording profiler runs for every benchmark.";
	}

	private static final class NoResult extends Result<NoResult> {
		private static final long serialVersionUID = 1L;

		private final String output;

		NoResult(final String output) {
			super(ResultRole.SECONDARY, "JFR", of(Double.NaN), "N/A", AggregationPolicy.SUM);
			this.output = output;
		}

		@Override
		protected Aggregator<NoResult> getThreadAggregator() {
			return new NoResultAggregator();
		}

		@Override
		protected Aggregator<NoResult> getIterationAggregator() {
			return new NoResultAggregator();
		}

		private static class NoResultAggregator implements Aggregator<NoResult> {

			@Override
			public NoResult aggregate(final Collection<NoResult> results) {
				StringBuilder agg = new StringBuilder();
				for (NoResult r : results) {
					agg.append(r.output);
				}
				return new NoResult(agg.toString());
			}
		}
	}

	@Override
	public String toString() {
		return "JFR";
	}

}
