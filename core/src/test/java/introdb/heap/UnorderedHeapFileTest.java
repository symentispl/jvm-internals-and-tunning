package introdb.heap;

import static java.util.Arrays.fill;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnorderedHeapFileTest {

  private Path heapFilePath;
  private Store heapFile;

  @BeforeEach
  public void setUp() throws IOException {
    heapFilePath = Files.createTempFile("heap", "0001");
    heapFile = new UnorderedHeapFile(heapFilePath, 1024, 4 * 1024);
  }

  @AfterEach
  public void tearDown() throws IOException {
    Files.delete(heapFilePath);
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
  void put_and_get_overflow_record() throws IOException, ClassNotFoundException {

    // given
    var firstkey = "1";
    var firstvalue = new byte[2048];
    fill(firstvalue, (byte) 1);

    var secondkey = "2";
    var secondvalue = new byte[2048];
    fill(firstvalue, (byte) 2);

    // when
    heapFile.put(newEntry(firstkey, firstvalue));
    heapFile.put(newEntry(secondkey, secondvalue));

    // then
    assertArrayEquals(firstvalue, (byte[]) heapFile.get(firstkey));
    assertArrayEquals(secondvalue, (byte[]) heapFile.get(secondkey));

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
    assertNull((byte[]) heapFile.get(key));

  }

  @Test
  void small_values_overflow_page() throws ClassNotFoundException, IOException {

    // given
    byte[] value = new byte[256];
    new Random().nextBytes(value);

    // when
    for (int i = 0; i < 1000; i++) {
      heapFile.put(new Entry(Integer.toString(i), value));
    }

    // then
    for (int i = 0; i < 1000; i++) {
      assertArrayEquals(value, (byte[]) heapFile.get(Integer.toString(i)));
    }

  }

  @Test
  void throw_exception_when_entry_too_large() throws ClassNotFoundException, IOException {

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
