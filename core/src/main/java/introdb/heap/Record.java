package introdb.heap;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Supplier;

class Record {

  private final Mark mark;
  private final byte[] key;
  private final byte[] value;

  Record(byte[] key, byte[] value) {
    this.key = key;
    this.value = value;
    this.mark = Mark.PRESENT;
  }

  Record(byte[] key, byte[] value, Mark mark) {
    this.key = key;
    this.value = value;
    this.mark = mark;
  }

  Mark mark() {
    return mark;
  }

  byte[] key() {
    return key;
  }

  byte[] value() {
    return value;
  }

  boolean isPresent() {
    return mark == Mark.PRESENT;
  }

  boolean isRemoved() {
    return mark == Mark.REMOVED;
  }

  int size() {
    return Byte.SIZE + Integer.SIZE + key.length + Integer.SIZE + value.length;
  }

  ByteBuffer write(ByteBuffer buffer) {

    buffer.put((byte) mark.mark());

    buffer.putInt(key.length);
    buffer.putInt(value.length);

    buffer.put(key);
    buffer.put(value);

    return buffer;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(key);
    result = prime * result + ((mark == null) ? 0 : mark.hashCode());
    result = prime * result + Arrays.hashCode(value);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Record other = (Record) obj;
    if (!Arrays.equals(key, other.key))
      return false;
    if (mark != other.mark)
      return false;
    if (!Arrays.equals(value, other.value))
      return false;
    return true;
  }

  static Record of(Entry entry) {
    var keyByteArrayOutput = new ByteArrayOutputStream();
    try (var objectOutput = new ObjectOutputStream(keyByteArrayOutput)) {
      objectOutput.writeObject(entry.key());
    } catch (IOException e) {
      throw new IOError(e);
    }

    var valueByteArrayOutput = new ByteArrayOutputStream();
    try (var objectOutput = new ObjectOutputStream(valueByteArrayOutput)) {
      objectOutput.writeObject(entry.value());
    } catch (IOException e) {
      throw new IOError(e);
    }

    return new Record(keyByteArrayOutput.toByteArray(), valueByteArrayOutput.toByteArray());
  }

  static Record read(Supplier<ByteBuffer> bufferSupplier) {
    var buffer = bufferSupplier.get();

    var keyLength = buffer.getInt();
    var valueLength = buffer.getInt();

    var key = new byte[keyLength];
    buffer.get(key);

    var value = new byte[valueLength];
    buffer.get(value);

    return new Record(key, value, Mark.PRESENT);
  }

  static void skip(Supplier<ByteBuffer> bufferSupplier) {
    var buffer = bufferSupplier.get();

    var keyLength = buffer.getInt();
    var valueLength = buffer.getInt();

    buffer.position(buffer.position() + keyLength + valueLength);
  }

  enum Mark {
    EMPTY((byte) 0), PRESENT((byte) 1), REMOVED((byte) 2);

    private final byte mark;

    Mark(byte mark) {
      this.mark = mark;
    }

    byte mark() {
      return mark;
    }

    static boolean isEmpty(byte b) {
      return EMPTY.mark == b;
    }

    static boolean isPresent(byte b) {
      return PRESENT.mark == b;
    }

    static boolean isRemoved(byte b) {
      return REMOVED.mark == b;
    }
  }

}
