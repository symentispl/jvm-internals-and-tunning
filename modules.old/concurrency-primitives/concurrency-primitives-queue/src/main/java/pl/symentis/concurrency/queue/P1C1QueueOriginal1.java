package pl.symentis.concurrency.queue;

public final class P1C1QueueOriginal1<E> {
 
  private final E[] buffer;
  private volatile long tail = 0;
  private volatile long head = 0;

  @SuppressWarnings("unchecked")
  public P1C1QueueOriginal1(final int capacity) {
    buffer = (E[]) new Object[capacity];
  }

  public boolean offer(final E e) {
    if (null == e)
      throw new NullPointerException("Null is not a valid element");

    final long currentTail = tail;
    final long wrapPoint = currentTail - buffer.length;
    if (head <= wrapPoint)
      return false;

    buffer[(int) (currentTail % buffer.length)] = e;
    tail = currentTail + 1;
    return true;
  }

  public E poll() {
    final long currentHead = head;
    if (currentHead >= tail)
      return null;

    final int index = (int) (currentHead % buffer.length);
    final E e = buffer[index];
    buffer[index] = null;
    head = currentHead + 1;
    return e;
  }
}