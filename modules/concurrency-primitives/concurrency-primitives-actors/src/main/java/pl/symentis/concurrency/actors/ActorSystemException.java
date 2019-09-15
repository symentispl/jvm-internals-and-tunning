package pl.symentis.concurrency.actors;

class ActorSystemException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 4015064292062371424L;

  ActorSystemException(Exception e) {
    super(e);
  }
  
}
