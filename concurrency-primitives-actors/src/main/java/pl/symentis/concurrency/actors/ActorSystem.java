package pl.symentis.concurrency.actors;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class ActorSystem {

  private final BlockingQueue<DefaultActorRef<? extends Actor>> actors = new LinkedBlockingQueue<>();
  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private volatile boolean running;

  public static void main(String[] args) {

    ActorSystem system = new ActorSystem();
    system.start();

    ActorRef<HelloWord> ref = system.spawn(HelloWord.class);

    ref.send("Hello world first time!!!");
    ref.send("Hello world second time!!!");
    ref.send("Hello world third time!!!");
    ref.send("Hello world fourth time!!!");
    
    system.shutdown();

  }

  void start() {
    running = true;
    int availableProcessors = Runtime.getRuntime().availableProcessors();
    System.out.println("starting actor system with "+availableProcessors);
    for (int i = 0; i < availableProcessors; i++) {
      executorService.submit(() -> {
        while (running) {
          try {
            var ref = actors.poll(1, TimeUnit.SECONDS);
            if (ref != null) {
              try {
                ref.schedule();
              } finally {
                actors.put(ref);
              }
            }
          } catch (InterruptedException e) {
            throw new ActorRefException(e);
          }
        }
      });
    }
  }

  void shutdown() {
    running = false;
    try {
      executorService.shutdown();
      executorService.awaitTermination(60, TimeUnit.SECONDS);
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  <T extends Actor> ActorRef<T> spawn(Class<T> clazz) {
    try {
      var actorRef = new DefaultActorRef<T>(clazz.getConstructor().newInstance());
      // schedule actor
      actors.offer(actorRef);
      return actorRef;
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      throw new ActorRefException(e);
    }
  }

}
