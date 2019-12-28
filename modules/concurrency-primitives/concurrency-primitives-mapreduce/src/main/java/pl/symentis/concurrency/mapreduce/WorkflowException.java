package pl.symentis.concurrency.mapreduce;

public class WorkflowException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -4252556210122585759L;

  public WorkflowException(Throwable e) {
    super(e);
  }
}
