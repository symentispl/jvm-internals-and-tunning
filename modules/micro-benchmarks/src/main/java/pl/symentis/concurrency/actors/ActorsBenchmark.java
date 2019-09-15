package pl.symentis.concurrency.actors;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class ActorsBenchmark {

  private ActorSystem system;
  private ActorRef<Counter> actorRef;


  @Setup(Level.Iteration)
  public void setUp() {
    system = new ActorSystem();
    system.start();
    actorRef = system.spawn(Counter.class);
  }
  
  @TearDown(Level.Iteration)
  public void tearDown() {
    system.shutdown();
  }
  
  @Benchmark
  public void send_to_actor_ref() {
    actorRef.send("Hello");
  }

}
