package introdb.heap;

import static java.util.Arrays.fill;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnorderedHeapFileTest {

	
	private UnorderedHeapFile heapFile;

	@BeforeEach
	public void setUp() throws IOException {
		Path heapFilePath = Files.createTempFile("heap", "0001");
		heapFile = new UnorderedHeapFile(heapFilePath, 1024, 4*1024);
	}
	
	@Test
	void put_and_get_second_record() throws IOException, ClassNotFoundException {
				
		// given 
		var firstkey = "1";
		var firstvalue = "value1";
		
		var secondkey = "2";
		var secondvalue = "value2";

		// when
		heapFile.put(newEntry(firstkey, firstvalue));
		heapFile.put(newEntry(secondkey,secondvalue));
		
		//than
		assertEquals(firstvalue, heapFile.get(firstkey));
		assertEquals(secondvalue, heapFile.get(secondkey));
		
	}

	@Test
	void put_and_get_overflow_record() throws IOException, ClassNotFoundException {
				
		// given 
		var firstkey = "1";
		var firstvalue = new byte[2048];
		fill(firstvalue, (byte)1);
		
		var secondkey = "2";
		var secondvalue = new byte[2048];
		fill(firstvalue, (byte)2);

		// when
		heapFile.put(newEntry(firstkey, firstvalue));
		heapFile.put(newEntry(secondkey,secondvalue));
		
		//than
		assertArrayEquals(firstvalue, (byte[])heapFile.get(firstkey));
		assertArrayEquals(secondvalue, (byte[])heapFile.get(secondkey));
		
	}

	@Test
	void put_and_update__record() throws IOException, ClassNotFoundException {
		
		// given
		var firstkey = "1";
		var firstvalue = "value1";
		var secondvalue = "value2";
		
		// when
		heapFile.put(newEntry(firstkey, firstvalue));
		heapFile.put(newEntry(firstkey,secondvalue));
		
		// than
		assertEquals(secondvalue, heapFile.get(firstkey));
		
	}

	@Test
	void put_and_update_overflow_record() throws IOException, ClassNotFoundException {
				
		// given 
		var firstkey = "1";
		var firstvalue = new byte[2048];
		fill(firstvalue, (byte)1);
		
		var secondvalue = new byte[2048];
		fill(secondvalue, (byte)2);

		// when
		heapFile.put(newEntry(firstkey, firstvalue));
		heapFile.put(newEntry(firstkey, secondvalue));
		
		//than
		assertArrayEquals(secondvalue, (byte[])heapFile.get(firstkey));
		
	}
	
	@Test
	void put_and_delete_record() throws IOException, ClassNotFoundException {
				
		// given 
		var firstkey = "1";
		var firstvalue = new byte[2048];
		new Random().nextBytes(firstvalue);
		
		// when
		byte[] actual = (byte[]) heapFile.remove(firstkey);
		
		// than
		assertNull(actual);
		
		// when
		heapFile.put(newEntry(firstkey, firstvalue));
		actual = (byte[]) heapFile.remove(firstkey);
		
		// than
		assertArrayEquals(actual, (byte[])heapFile.get(firstkey));
		
	}

	@Test
	public void fill_heap_file() throws ClassNotFoundException, IOException {

		// given 
		byte[] value = new byte[256];
		new Random().nextBytes(value);
		
		// when
		for(int i=0;i<1000;i++) {
			heapFile.put(new Entry(Integer.toString(i),value ));			
		}
		
		// than
		for(int i=0;i<1000;i++) {
			assertArrayEquals(value,(byte[])heapFile.get(Integer.toString(i)));			
		}
	
	}

	private Entry newEntry(Serializable firstkey, Serializable firstvalue) {
		Entry entry = new Entry(firstkey,firstvalue);
		return entry;
	}
	
}
