package introdb.record;

import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

import introdb.api.Entry;

public class TransientRecord implements Record {

	private static final Logger LOGGER = Logger.getLogger(TransientRecord.class.getName());
	
	public static TransientRecord of(Entry entry) {
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

		return new TransientRecord(keyByteArrayOutput.toByteArray(), valueByteArrayOutput.toByteArray());
	}

	private final Record.Mark mark;
	private final byte[] key;
	private final byte[] value;

	TransientRecord(byte[] key, byte[] value) {
		this.key = key;
		this.value = value;
		this.mark = Record.Mark.PRESENT;
	}

	@Override
	public Record.Mark mark() {
		return mark;
	}

	@Override
	public byte[] key() {
		return key;
	}

	@Override
	public byte[] value() {
		return value;
	}

	public ByteBuffer write(ByteBuffer buffer) {

		LOGGER.fine( ()->format("writing record %s at position %d to buffer %s", this, buffer.position(), buffer));

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
		TransientRecord other = (TransientRecord) obj;
		if (!Arrays.equals(key, other.key))
			return false;
		if (mark != other.mark)
			return false;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TransientRecord [mark=").append(mark)
			   .append(", key=").append(key)
			   .append(", value=").append(value)
			   .append("]");
		return builder.toString();
	}

	public static byte[] serializeKey(Serializable key) {
		var keyByteArrayOutput = new ByteArrayOutputStream();
		try (var objectOutput = new ObjectOutputStream(keyByteArrayOutput)) {
			objectOutput.writeObject(key);
		} catch (IOException e) {
			throw new IOError(e);
		}
		return keyByteArrayOutput.toByteArray();
	}

}
