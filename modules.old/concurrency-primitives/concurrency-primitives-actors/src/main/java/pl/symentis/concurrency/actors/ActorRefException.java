package pl.symentis.concurrency.actors;

class ActorRefException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  ActorRefException(Exception e) {
    super(e);
  }


}
