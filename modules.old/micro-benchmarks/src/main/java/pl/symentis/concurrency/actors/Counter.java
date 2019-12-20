package pl.symentis.concurrency.actors;

public class Counter implements Actor {

  @SuppressWarnings("unused")
  private int counter;

  @Override
  public void receive(Object message) {
    counter++;
  }

}
