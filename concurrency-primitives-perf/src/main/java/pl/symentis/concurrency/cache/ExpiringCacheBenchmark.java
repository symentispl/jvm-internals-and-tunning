package pl.symentis.concurrency.cache;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class ExpiringCacheBenchmark {

  private ExpiringCache<String,String> cache;
  private long c;
  
  @Setup(Level.Iteration)
  public void setUp() {
    c=0;
    cache = new ExpiringCache<>(5, TimeUnit.SECONDS);
  }

  @TearDown(Level.Iteration)
  public void tearDown() {
    cache.shutdown();
  }

  @Benchmark
  @Group("cache")
  public void put_to_cache() {
    cache.put("Hello","World!!!");
  }

  @Benchmark
  @Group("cache")
  public String get_from_cache() {
    return cache.get("Hello");
  }

  @Benchmark
  @Group("unique_put")
  public void put_unique_to_cache() {
    cache.put("Hello"+Long.toString(c++),"World!!!");
  }

  @Benchmark
  @Group("unique_put")
  public String get_unique_from_cache() {
    return cache.get("Hello0");
  }
}
