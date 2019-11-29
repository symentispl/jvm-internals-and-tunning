package pl.symentis.jmh.profilers;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.ExternalProfiler;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.*;
import org.openjdk.jmh.runner.IterationType;
import pl.project13.scala.jmh.extras.profiler.ProfilerUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static java.lang.String.format;

public final class JFR implements ExternalProfiler, InternalProfiler {

    private volatile Path filename;
    private volatile boolean afterWarmup;
    private Path outputDir;

    public JFR(String initLine) throws Exception {
        OptionParser parser = new OptionParser();
        OptionSpec<String> outputDir = parser.accepts("dir", "Output directory").withRequiredArg()
                .describedAs("directory").ofType(String.class);

        OptionSet optionSet = ProfilerUtils.parseInitLine(initLine, parser);

        if (optionSet.has(outputDir)) {
            this.outputDir = Paths.get(optionSet.valueOf(outputDir));
            Files.createDirectories(this.outputDir);
        }
    }

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

        if (!afterWarmup && iterationParams.getType() == IterationType.MEASUREMENT) {
            afterWarmup = true;

            if (outputDir == null) {
                try {
                    outputDir = Files.createTempDirectory(benchmarkParams.id());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            Path jcmd = Paths.get(benchmarkParams.getJvm()).getParent().resolve("jcmd");
            filename = outputDir.resolve("profile.jfr").toAbsolutePath();
            try {
                int exitCode = new ProcessBuilder()
                        .command(jcmd.toString(), Long.toString(ProcessHandle.current().pid()), "JFR.start",
                                format("filename=%s dumponexit=true disk=true settings=profile", filename))
                        .start().waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("failed to run jcmd command");
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams,
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
