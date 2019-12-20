package pl.symentis.concurrency.actors;

interface ActorRef<T extends Actor> {

  void send(Object message);

}
