package pl.symentis.jvm.concurrency.lockfree;

import java.util.concurrent.atomic.AtomicLong;

public class LockFreeLinkedList<V> {

	private class Node {
		Node next;
	}

	private class AuxiliaryNode extends Node {
	}

	private class NormalNode extends Node {
		AtomicLong refct = new AtomicLong(0);
		V data;
		
		V safeRead(){
			while(true){
				V q = data;
				if(q==null){
					return null;
				}
				refct.incrementAndGet();
				if(q==data){
					return q;
				}
				release(q);
			}
		}

		private void release(V q) {
			long c = refct.getAndDecrement();
			if(c>1){
				return;
			}
		}
	}

	private class Cursor {
		Node target;
		AuxiliaryNode pre_aux;
		NormalNode pre_cell;

		boolean isValid() {
			return pre_aux == target;
		}
	}

	private final NormalNode first;
	private final NormalNode last;

	public LockFreeLinkedList() {
		first = new NormalNode();
		AuxiliaryNode auxiliaryNode = new AuxiliaryNode();
		first.next = auxiliaryNode;
		last = new NormalNode();
		auxiliaryNode.next = last;
	}

}
