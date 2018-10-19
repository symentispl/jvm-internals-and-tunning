package introdb.structures;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

class FirstFitFreeList{

    final int pageSize = 4*1024;
    final ConcurrentSkipListSet<HeapBlock> freeList = new ConcurrentSkipListSet<>(new HeapBlockComparator());

    FirstFitFreeList(int numberOfPages) {
        freeList.addAll(range(0,numberOfPages).mapToObj(i-> new HeapBlock(i,0,pageSize)).collect(toList()));
    }

    boolean isEmpty() {
        return freeList.isEmpty();
    }

    int available() {
        return freeList.stream().mapToInt(b -> b.free).sum();
    }

    Optional<HeapBlock> allocate(int recordSize){

        // simplest possible first fit algorithm implementation
        Iterator<HeapBlock> iterator = freeList.iterator();
        while(iterator.hasNext()){
            HeapBlock block =iterator.next();
            if(recordSize<=block.free){
                iterator.remove();
                HeapBlock allocatedBlock = split(recordSize, block);
                return Optional.of(allocatedBlock);
            }
        }
        return Optional.empty();
    }

    void free(HeapBlock block){
        HeapBlock floor = freeList.floor(block);
        HeapBlock ceiling = freeList.ceiling(block);

        int free =block.free;
        int pageOffset = block.pageOffset;
        boolean merge = false;
        if(ceiling!=null && ceiling.pageNr==block.pageNr){
            free += ceiling.free;
            freeList.remove(ceiling);
            merge=true;
        }

        if(floor!=null && floor.pageNr==block.pageNr) {
            free += floor.free;
            freeList.remove(floor);
            merge=true;
        }

        if(merge){
            freeList.add(new HeapBlock(block.pageNr, pageOffset, free));
        } else {
            freeList.add(block);
        }
    }

    private HeapBlock split(int recordSize, HeapBlock block) {
        HeapBlock allocatedBlock = new HeapBlock(block.pageNr,block.pageOffset,recordSize);

        if(block.free-recordSize>0){
            HeapBlock freeBlock = new HeapBlock(block.pageNr,block.pageOffset+recordSize, block.free-recordSize);
            freeList.add(freeBlock);
        }

        return allocatedBlock;
    }

    private class HeapBlockComparator implements java.util.Comparator<HeapBlock> {
        @Override
        public int compare(HeapBlock o1, HeapBlock o2) {
            return ((o1.pageNr*pageSize)+o1.pageOffset)-((o2.pageNr*pageSize)+o2.pageOffset);
        }
    }

}

class HeapBlock {

    final int pageNr;
    final int pageOffset;
    final int free;

    HeapBlock(int pageNr, int pageOffset, int free){
        this.pageNr=pageNr;
        this.pageOffset =pageOffset;
        this.free  = free;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeapBlock heapBlock = (HeapBlock) o;
        return pageNr == heapBlock.pageNr &&
                pageOffset == heapBlock.pageOffset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNr, pageOffset);
    }

    @Override
    public String toString() {
        return "HeapBlock{" +
                "pageNr=" + pageNr +
                ", pageOffset=" + pageOffset +
                ", free=" + free +
                '}';
    }
}