package introdb.structures;

import org.junit.Test;

import introdb.structures.FirstFitFreeList;
import introdb.structures.HeapBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.Assert.*;

public class FirstFitFreeListTest {


    @Test
    public void allocate_and_free_page_size_blocks(){
        FirstFitFreeList freeList = new FirstFitFreeList(1024);

        List<Optional<HeapBlock>> allocatedPages = new ArrayList<>(1024);
        for(int i=0;i<1024;i++) {
            allocatedPages.add(freeList.allocate(4 * 1024));
        }

        assertTrue("free list is not empty",freeList.isEmpty());
        assertEquals("allocated pages contain duplicates",1024, allocatedPages.stream().map(Optional::get).distinct().count());

        allocatedPages.stream().forEach(o -> o.ifPresent(freeList::free));

        assertFalse(freeList.isEmpty());
        assertEquals(1024*4*1024, freeList.available());

    }

    @Test
    public void allocate_and_free_half_page_size_blocks(){
        FirstFitFreeList freeList = new FirstFitFreeList(1024);

        List<Optional<HeapBlock>> allocatedPages = new ArrayList<>(1024);
        for(int i=0;i<1024;i++) {
            allocatedPages.add(freeList.allocate(2 * 1024));
        }

        assertFalse("free list is empty",freeList.isEmpty());
        assertEquals("allocated pages contain duplicates",1024, allocatedPages.stream().map(Optional::get).distinct().count());

        allocatedPages.stream().forEach(o -> o.ifPresent(freeList::free));

        assertFalse(freeList.isEmpty());
        assertEquals(1024*4*1024, freeList.available());

    }

    @Test
    public void allocate_and_free_random_page_size_blocks(){

        Random rand = new Random();

        FirstFitFreeList freeList = new FirstFitFreeList(1024);

        List<Optional<HeapBlock>> allocatedPages = new ArrayList<>(1024);
        for(int i=0;i<1024;i++) {
            allocatedPages.add(freeList.allocate(rand.nextInt(4 * 1024)));
        }

        assertFalse("free list is empty",freeList.isEmpty());
        assertEquals("allocated pages contain duplicates",1024, allocatedPages.stream().map(Optional::get).distinct().count());

        allocatedPages.stream().forEach(o -> o.ifPresent(freeList::free));

        assertFalse(freeList.isEmpty());
        assertEquals(1024*4*1024, freeList.available());

    }
}