package introdb.structures;

import java.util.Comparator;

public class BinarySearchTree<T> {

	private Comparator<T> comparator;
	private Node root;

	class Node {
		private final T value;
		private Node left;
		private Node right;
		
		Node(T value) {
			this.value = value;
		}
	}
	
	public BinarySearchTree(Comparator<T> comparator) {
		this.comparator = comparator;
	}
	
	private Node addRecursive(Node current, T value) {
	    if (current == null) {
	        return new Node(value);
	    }
	 
	    if (comparator.compare(value,current.value)<0) {
	        current.left = addRecursive(current.left, value);
	    } else if (comparator.compare(value,current.value)>0) {
	        current.right = addRecursive(current.right, value);
	    } else {
	        return current;
	    }
	 
	    return current;
	}
	
	public void add(T value) {
	    root = addRecursive(root, value);
	}
	
}
