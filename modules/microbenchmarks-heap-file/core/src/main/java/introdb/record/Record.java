package introdb.record;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public interface Record {

	enum Mark {
		EMPTY((byte) 0), PRESENT((byte) 1), REMOVED((byte) 2);
	
		private final byte mark;
	
		Mark(byte mark) {
			this.mark = mark;
		}
	
		public byte mark() {
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

	byte[] value();

	byte[] key();

	Record.Mark mark();

	default boolean isPresent() {
		return mark() == Record.Mark.PRESENT;
	}

	default boolean isRemoved() {
		return mark() == Record.Mark.REMOVED;
	}
	
	default boolean isEmpty() {
		return mark() == Record.Mark.EMPTY;
	}

	default int size() {
		return (Byte.BYTES+ Integer.BYTES+ Integer.BYTES) + key().length + value().length;
	}
	
	default Object valueAsObject() throws IOException, ClassNotFoundException {
		try (var input = new ObjectInputStream(new ByteArrayInputStream(value()))) {
			return input.readObject();
		}
	}

}
