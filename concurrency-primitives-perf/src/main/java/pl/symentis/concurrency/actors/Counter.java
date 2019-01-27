package pl.symentis.concurrency.actors;

public class Counter implements Actor {

  private int counter;

  @Override
  public void receive(Object message) {
    counter++;
  }

}
