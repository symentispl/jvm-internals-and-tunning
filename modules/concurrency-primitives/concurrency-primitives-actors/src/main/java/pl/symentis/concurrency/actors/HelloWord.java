package pl.symentis.concurrency.actors;

public class HelloWord implements Actor {

  @Override
  public void receive(Object message) {
    System.out.println(message);
  }

}
