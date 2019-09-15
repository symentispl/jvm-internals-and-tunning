package pl.symentis.concurency.linkedlist;


import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Lock-Free Linked List as described in Timothy L. Harris's paper
 * <a href="http://research.microsoft.com/pubs/67089/2001-disc.pdf">A pragmatic
 * implementation of non-blocking linked-lists</a>
 */
public class HarrisLinkedList<E extends Comparable<E>> {

  final Node<E> head;
  final Node<E> tail;

  static class Node<E> {
    final E key;
    final AtomicMarkableReference<Node<E>> next;

    Node(E key) {
      this.key = key;
      this.next = new AtomicMarkableReference<Node<E>>(null, false);
    }
  }

  static class Window<T> {
    Node<T> pred;
    Node<T> curr;

    Window(Node<T> pred, Node<T> curr) {
      this.pred = pred;
      this.curr = curr;
    }
  }

  public HarrisLinkedList() {
    tail = new Node<E>(null);
    head = new Node<E>(null);
    head.next.set(tail, false);
  }

  public boolean add(E key) {
    final Node<E> newNode = new Node<E>(key);
    while (true) {
      final Window<E> window = find(key);
      final Node<E> left_node = window.pred;
      final Node<E> right_node = window.curr;
      if (right_node.key == key) {
        return false;
      } else {
        newNode.next.set(right_node, false);
        if (left_node.next.compareAndSet(right_node, newNode, false, false)) {
          return true;
        }
      }
    }
  }

  public boolean remove(E key) {
    while (true) {
      final Window<E> window = find(key);
      final Node<E> left_node = window.pred;
      final Node<E> right_node = window.curr;
      if (right_node.key != key) {
        return false;
      }
      final Node<E> succ = right_node.next.getReference();
      if (!right_node.next.compareAndSet(succ, succ, false, true)) {
        continue;
      }
      left_node.next.compareAndSet(right_node, succ, false, false);
      return true;
    }
  }

  private Window<E> find(E key) {
    Node<E> left_node = null;
    Node<E> right_node = null;
    Node<E> succ = null;
    boolean[] marked = { false };
    
    if (head.next.getReference() == tail) {
      return new Window<E>(head, tail);
    }

    retry: while (true) {
      left_node = head;
      right_node = left_node.next.getReference();
      while (true) {
        succ = right_node.next.get(marked);
        while (marked[0]) {
          if (!left_node.next.compareAndSet(right_node, succ, false, false)) {
            continue retry;
          }
          right_node = succ;
          succ = right_node.next.get(marked);
        }

        if (right_node == tail || key.compareTo(right_node.key) <= 0) {
          return new Window<E>(left_node, right_node);
        }
        left_node = right_node;
        right_node = succ;
      }
    }
  }

  public boolean contains(E key) {
    boolean[] marked = { false };
    Node<E> right_node = head.next.getReference();
    right_node.next.get(marked);
    while (right_node != tail && key.compareTo(right_node.key) > 0) {
      right_node = right_node.next.getReference();
      right_node.next.get(marked);
    }
    return (right_node.key == key && !marked[0]);
  }
}