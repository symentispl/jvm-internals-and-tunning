package introdb.heap;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import introdb.api.Entry;
import introdb.api.KeyValueStorage;
import introdb.fs.FileChannelBlockFile;
import introdb.pagecache.HashMapPageCache;
import introdb.pagecache.PageCacheBlockFile;

class UnorderedHeapFileTest {

	Path heapFilePath;
	KeyValueStorage heapFile;

	@BeforeEach
	public void setUp() throws IOException {
		heapFilePath = Files.createTempFile("heap", "0001");
		FileChannelBlockFile fileChannelBlockFile = new FileChannelBlockFile(
				FileChannel.open(heapFilePath, StandardOpenOption.READ, StandardOpenOption.WRITE), 4 * 1024);
		heapFile = new UnorderedHeapFile(new PageCacheBlockFile( new HashMapPageCache(), fileChannelBlockFile));
	}

	@AfterEach
	public void tearDown() throws IOException {
		Files.delete(heapFilePath);
	}

	@Test
	void put_and_get_record() throws IOException, ClassNotFoundException {
	
		// given
		var firstkey = "1";
		var firstvalue = "value1";
	
		// when
		heapFile.put(newEntry(firstkey, firstvalue));
	
		// then
		assertEquals(firstvalue, heapFile.get(firstkey));
	
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
		heapFile.put(newEntry(secondkey, secondvalue));
	
		// then
		assertEquals(firstvalue, heapFile.get(firstkey));
		assertEquals(secondvalue, heapFile.get(secondkey));
	
	}

	@Test
	void put_and_update_record() throws IOException, ClassNotFoundException {
	
		// given
		var key = "1";
		var firstvalue = "value1";
		var secondvalue = "value2";
	
		// when
		heapFile.put(newEntry(key, firstvalue));
		heapFile.put(newEntry(key, secondvalue));
	
		// then
		assertEquals(secondvalue, heapFile.get(key));
	
	}

	@Test
	void remove_unexisting_record_returns_null() throws ClassNotFoundException, IOException {
		// given
		var key = "1";
	
		// when
		byte[] actual = (byte[]) heapFile.remove(key);
	
		// then
		assertNull(actual);
	}

	@Test
	void put_and_delete_record() throws IOException, ClassNotFoundException {
	
		// given
		var key = "1";
		var value = new byte[2048];
		new Random().nextBytes(value);
	
		// when
		heapFile.put(newEntry(key, value));
		heapFile.remove(key);
	
		// then
		assertNull(heapFile.get(key));
	
	}

	@ParameterizedTest
	@ValueSource(ints = { 256, 2048 })
	void insert_unique_keys_overflow_single_block(int valueSize) {
	
		// given
		List<Entry> entries = IntStream.range(0, 1000).mapToObj(i -> {
			byte[] value = new byte[valueSize];
			new Random().nextBytes(value);
			return new Entry(Integer.toString(i), value);
		}).collect(toList());
	
		// when
		entries.forEach(entry -> {
			try {
				heapFile.put(entry);
			} catch (IOException e) {
				fail("cannot put entry %s", e);
			}
		});
	
		// then
		entries.forEach(entry -> {
			byte[] value = null;
			try {
				value = (byte[]) heapFile.get(entry.key());
			} catch (ClassNotFoundException | IOException e) {
				fail("cannot get entry %s", e);
			}
			assertThat(value).isEqualTo(entry.value());
		});
	
	}

	@ParameterizedTest
	@ValueSource(ints = { 256, 2048 })
	void insert_same_key_overflow_single_block(int valueSize) throws Exception {
	
		// given
		List<Entry> entries = IntStream.range(0, 1000).mapToObj(i -> {
			byte[] value = new byte[valueSize];
			new Random().nextBytes(value);
			return new Entry("1", value);
		}).collect(toList());
	
		// when
		entries.forEach(entry -> {
			try {
				heapFile.put(entry);
			} catch (IOException e) {
				fail("cannot put entry %s", e);
			}
		});
	
		// then
		var value = (byte[]) heapFile.get("1");
		assertThat(value).isEqualTo(entries.get(999).value());
	
	}

	@ParameterizedTest
	@ValueSource(ints = { 256, 2048 })
	void delete_all_keys_overflow_single_block(int valueSize) throws Exception {
	
		// given
		List<Entry> entries = IntStream.range(0, 1000).mapToObj(i -> {
			byte[] value = new byte[valueSize];
			new Random().nextBytes(value);
			return new Entry(i, value);
		}).collect(toList());
	
		entries.forEach(entry -> {
			try {
				heapFile.put(entry);
			} catch (IOException e) {
				fail("cannot put entry %s", e);
			}
		});
	
		// when
		entries.forEach(entry -> {
			try {
				heapFile.remove(entry.key());
			} catch (IOException | ClassNotFoundException e) {
				fail("cannot remove entry", e);
			}
		});
	
		// then
		entries.forEach(entry -> {
			try {
				assertThat(heapFile.get(entry.key())).isNull();
			} catch (ClassNotFoundException | IOException e) {
				fail("cannot get entry", e);
			}
		});
	
	}

	@Test
	void throw_exception_when_entry_too_large() {
	
		// given
		byte[] value = new byte[4 * 1024];
		new Random().nextBytes(value);
	
		assertThatThrownBy(() -> {
			// when
			heapFile.put(new Entry("0", value));
		}).isInstanceOf(IllegalArgumentException.class);
	
	}

	private Entry newEntry(Serializable key, Serializable value) {
		return new Entry(key, value);
	}

}
