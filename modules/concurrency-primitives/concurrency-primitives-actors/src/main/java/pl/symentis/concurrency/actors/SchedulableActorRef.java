package pl.symentis.concurrency.actors;

import java.util.concurrent.ArrayBlockingQueue;

class SchedulableActorRef<T extends Actor> implements ActorRef<T> {

  private final T instance;
  private final ArrayBlockingQueue<Object> mailbox;

  SchedulableActorRef(T newInstance) {
    this.instance = newInstance;
    this.mailbox = new ArrayBlockingQueue<>(16);
  }

  @Override
  public void send(Object message) {
    try {
      mailbox.put(message);
    } catch (InterruptedException e) {
      throw new ActorRefException(e);
    }
  }

  void onSchedule() {
    Object message = mailbox.poll();
    if (message != null) {
      instance.receive(message);
    }
  }

}
