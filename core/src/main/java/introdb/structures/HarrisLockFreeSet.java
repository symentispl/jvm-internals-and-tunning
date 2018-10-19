package introdb.structures;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicMarkableReference;

class HarrisLockFreeSet<T> implements Iterable<T>{


    final Node head = new Node();
    final Node tail = new Node();
    final Comparator<T> comparator;


    HarrisLockFreeSet(Comparator<T> comparator){
        this.comparator = comparator;
        head.next.set(tail, false);
    }

    boolean insert(T key){
        Node new_node = new Node(key);

        do{
            Pair pair = search (key);
            if(pair.right!=tail && pair.right.key==key){
                return false;
            }
            new_node.next.set(pair.right,false);

            if(pair.left.next.compareAndSet(pair.right,new_node,false,false)){
                return true;
            }
        } while(true);
    }

    public Iterator<T> iterator(){

        return new Iterator<T>() {

            Node current = head;

            @Override
            public boolean hasNext() {

                do{
                    Node unmarked_reference = get_unmarked_reference(current.next);
                    if(unmarked_reference!=null){
                        return unmarked_reference.key!=null;
                    }
                    current = unmarked_reference.next.getReference();
                } while(current!=tail);

                return current.next.getReference().key != null;
            }

            @Override
            public T next() {
                do{
                    Node unmarked_reference = get_unmarked_reference(current.next);
                    if(unmarked_reference!=null && unmarked_reference.key!=null){
                        return unmarked_reference.key;
                    }
                    current = unmarked_reference.next.getReference();

                } while(current!=tail);

                throw new NoSuchElementException();
            }
        };
    }

    boolean delete(T search_key){
        AtomicMarkableReference<Node> right_node_next;

        do{
            Pair pair = search(search_key);
            if(pair.right == tail || pair.right.key!=search_key){
                return false;
            }
            right_node_next = pair.right.next;
            if(right_node_next.compareAndSet(right_node_next.getReference(), right_node_next.getReference(), false, true)){
                break;
            }
        } while(true);
        return true;
    }


    private Pair search(T search_key){
        Node left_node = null;
        AtomicMarkableReference<Node> left_node_next = null;
        Node right_node;

        search_again:
        do{
            Node t = head;
            AtomicMarkableReference<Node> t_next = head.next;

            do{
                if(!t_next.isMarked()){
                    left_node = t;
                    left_node_next = t_next;
                }
                t = get_unmarked_reference(t_next);
                if(t==tail) break;
                t_next = t.next;

            } while (t_next.isMarked() || comparator.compare(t.key,search_key)<0);

            right_node = t;
            if (left_node_next.getReference() == right_node){
                if( right_node!=tail && right_node.next.isMarked()){
                    break search_again;
                } else{
                    return new Pair(left_node,right_node);
                }
            }

            if(left_node_next.compareAndSet(left_node,right_node,true,false)){
                if(right_node!=tail){
                    break search_again;
                } else{
                    return new Pair(left_node,right_node);
                }
            }

        } while(true);
        return null;
    }

    private Node get_unmarked_reference(AtomicMarkableReference<Node> t_next) {
        boolean[] markHolder = new boolean[1];
        Node v = t_next.get(markHolder);
        if(!markHolder[0]){
            return v;
        }
        return null;
    }

    private Node get_marked_reference(AtomicMarkableReference<Node> t_next) {
        boolean[] markHolder = new boolean[1];
        Node v = t_next.get(markHolder);
        if(markHolder[0]){
            return v;
        }
        return null;
    }

    private class Node {
        T key;
        final AtomicMarkableReference<Node> next = new AtomicMarkableReference<>(null,false);

        Node() {
        }

        Node(T key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "key=" + key +
                    ",next=" + next.getReference().key +
                    ", marked=" + next.isMarked() +
                    '}';
        }
    }

    private class Pair{

        final Node left;
        final Node right;

        Pair(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
        }
    }


    public static void main(String[] argv){
        HarrisLockFreeSet<Integer> set = new HarrisLockFreeSet<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });

        set.insert(0);
        set.insert(1);
        set.insert(2);
        set.insert(3);

        set.delete(2);

        for(Integer t:set){
            System.out.println(t);
        }

    }

}
