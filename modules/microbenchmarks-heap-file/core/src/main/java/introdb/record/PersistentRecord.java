package introdb.record;

import static java.lang.String.format;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class PersistentRecord implements Record{

	private static final Logger LOGGER = Logger.getLogger(PersistentRecord.class.getName());
	
	public static PersistentRecord read(ByteBuffer buffer) {
		
		var offset = buffer.position();
		LOGGER.fine( () -> format("reading record at offset %d", offset));
		var mark = buffer.get();
		LOGGER.finer( () -> format("found record, mark is %s", mark));

		if (Record.Mark.isEmpty(mark)) {
			return new PersistentRecord(buffer, Record.Mark.EMPTY, offset, -1, -1);
		}

		var keyLength = buffer.getInt();
		var valueLength = buffer.getInt();
		
		// persistent record is lazy, it only reads data it needs
		// it can initialize key and value later
		// but it needs to move buffer to next position
		buffer.position(buffer.position()+keyLength+valueLength);

		if (Record.Mark.isRemoved(mark)) {
			return new PersistentRecord(buffer, Record.Mark.REMOVED, offset, keyLength, valueLength);
		}

		return new PersistentRecord(buffer, Record.Mark.PRESENT, offset, keyLength, valueLength);
	}

	private final ByteBuffer buffer;
	private Record.Mark mark;
	private final int offset;
	private final int keyLength;
	private final int valueLength;

	public PersistentRecord(ByteBuffer buffer, Record.Mark mark, int offset, int keyLength, int valueLength) {
		super();
		this.mark = mark;
		this.buffer = buffer;
		this.offset = offset;
		this.keyLength = keyLength;
		this.valueLength = valueLength;
	}

	@Override
	public byte[] value() {
		var value = new byte[valueLength];
		buffer.asReadOnlyBuffer()
		      .position(
				offset
				+Byte.BYTES // mark
				+Integer.BYTES // key length
				+Integer.BYTES // value length
				+keyLength) //key
		      .get(value);
		return value;
	}

	@Override
	public byte[] key() {
		var key = new byte[keyLength];
		int position = offset
					+Byte.BYTES // mark
					+Integer.BYTES // key length
					+Integer.BYTES; // value length
		var roBuffer = buffer.asReadOnlyBuffer();
		LOGGER.fine( () -> format("reading key of record at offset %d and position %d from buffer %s", offset, position, roBuffer));
		roBuffer.position(position).get(key);
		return key;
	}

	@Override
	public Record.Mark mark() {
		return mark;
	}

	public int offset() {
		return offset;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersistentRecord [buffer=").append(buffer)
			   .append(", mark=").append(mark)
			   .append(", offset=").append(offset)
			   .append(", keyLength=").append(keyLength)
			   .append(", valueLength=").append(valueLength)
			   .append("]");
		return builder.toString();
	}

}