package introdb.heap;

import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import introdb.heap.Record.Mark;

class UnorderedHeapFile implements Store, Iterable<Record> {

  private final PageCache pageCache;
  private final int maxNrPages;
  private final int pageSize;

  private ByteBuffer lastPage;
  private int lastPageNumber = -1;

  private final FileChannel file;

  UnorderedHeapFile(Path path, int maxNrPages, int pageSize) throws IOException {
    this.file = FileChannel.open(path,
        Set.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE));
    this.pageCache = new PageCache(maxNrPages, this::readPage);
    this.maxNrPages = maxNrPages;
    this.pageSize = pageSize;
    nextPage();
  }

  @Override
  public void put(Entry entry) {

    assertTooManyPages();

    var newRecord = Record.of(entry);

    assertRecordSize(newRecord);

    var iterator = cursor();
    while (iterator.hasNext()) {
      var record = iterator.next();
      if (Arrays.equals(newRecord.key(), record.key())) {
        iterator.remove();
        break;
      }
    }

    if (lastPage.remaining() < newRecord.size()) {
      nextPage();
    }

    var src = newRecord.write(lastPage);

    flushPage(src, lastPageNumber);
  }

  private void nextPage() {
    lastPage = ByteBuffer.allocate(pageSize);
    lastPageNumber++;
    pageCache.put(lastPageNumber, lastPage);
  }

  @Override
  public Object get(Serializable key) {

    // serialize key
    var keySer = serializeKey(key);

    var iterator = cursor();
    while (iterator.hasNext()) {
      var record = iterator.next();
      if (Arrays.equals(keySer, record.key())) {
        return deserializeValue(record.value());
      }
    }
    return null;
  }

  public Object remove(Serializable key) {
    // serialize key
    var keySer = serializeKey(key);

    var iterator = cursor();
    while (iterator.hasNext()) {
      var record = iterator.next();
      if (Arrays.equals(keySer, record.key())) {
        var value = deserializeValue(record.value());
        iterator.remove();
        return value;
      }
    }
    return null;
  }

  private void flushPage(ByteBuffer page, int pageNr) {
    int position = page.position();
    page.rewind();
    try {
      file.write(page, pageNr * pageSize);
    } catch (IOException e) {
      throw new IOError(e);
    }
    page.position(position);
  }

  private ByteBuffer readPage(int pageNr) {

    if (pageNr > lastPageNumber) {
      return null;
    }

    ByteBuffer page = ByteBuffer.allocate(pageSize);
    try {
      var bytesRead = file.read(page, pageNr * pageSize);
      if (bytesRead != pageSize) {
        throw new IllegalStateException(format("read page size is %d, file corrupted", bytesRead));
      }
      return page;
    } catch (IOException e) {
      throw new IOError(e);
    }

  }

  private void assertRecordSize(Record newRecord) {
    if (newRecord.size() > pageSize) {
      throw new IllegalArgumentException("entry to big");
    }
  }

  private void assertTooManyPages() {
    if (lastPageNumber >= maxNrPages) {
      throw new TooManyPages();
    }
  }

  private static Object deserializeValue(byte[] value) {
    try (var inputStream = new ByteArrayInputStream(value)) {
      try (var objectInput = new ObjectInputStream(inputStream)) {
        return objectInput.readObject();
      }
    } catch (IOException | ClassNotFoundException e) {
      throw new IOError(e);
    }
  }

  private static byte[] serializeKey(Serializable key) {
    try (var byteArray = new ByteArrayOutputStream()) {
      try (var objectOutput = new ObjectOutputStream(byteArray)) {
        objectOutput.writeObject(key);
      }
      return byteArray.toByteArray();
    } catch (IOException e) {
      throw new IOError(e);
    }
  }

  class Cursor implements Iterator<Record> {

    int pageNr;
    ByteBuffer currentPage;
    boolean hasNext;
    private int inPagePosition = -1;

    @Override
    public boolean hasNext() {
      if (!hasNext) {
        inPagePosition = -1;
        do {
          if (currentPage == null) {
            var page = pageCache.get(pageNr);
            if (page == null) {
              return hasNext = false;
            }
            currentPage = page.duplicate().rewind();
          }

          byte mark = 0;
          do {
            mark = currentPage.get();
            if (Mark.isPresent(mark)) {
              return hasNext = true;
            }
            if (Mark.isRemoved(mark)) {
              skip();
            }
          } while (!Mark.isEmpty(mark) || !currentPage.hasRemaining());

          currentPage = null;
          pageNr++;

        } while (true);
      }
      return hasNext;
    }

    @Override
    public Record next() {
      if (hasNext || hasNext()) {
        hasNext = false;
        inPagePosition = currentPage.position() - 1;
        return record();
      }
      throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      if (inPagePosition < 0) {
        throw new IllegalStateException(
            "next() method has not yet been called, or the remove() method has already been called");
      }
      currentPage.put(inPagePosition, Mark.REMOVED.mark());
      // force page flush
      flushPage(currentPage, pageNr);
      inPagePosition = -1;
    }

    Optional<ByteBuffer> page() {
      return Optional.ofNullable(currentPage);
    }

    private Record record() {
      return Record.read(() -> currentPage);
    }

    private void skip() {
      Record.skip(() -> currentPage);
    }

  }

  Cursor cursor() {
    return new Cursor();
  }

  @Override
  public Iterator<Record> iterator() {
    return cursor();
  }

}
