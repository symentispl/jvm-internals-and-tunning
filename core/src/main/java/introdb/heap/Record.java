package introdb.heap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

class Record {

	enum Mark {
		EMPTY,
		PRESENT,
		REMOVED
	}

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
		return mark==Mark.PRESENT;
	}

	boolean isRemoved() {
		return mark==Mark.REMOVED;
	}

	int size() {
		return Byte.SIZE+Integer.SIZE+key.length+Integer.SIZE+value.length;
	}

	ByteBuffer writeExternal(Supplier<ByteBuffer> bufferSupplier) throws IOException {
		
		ByteBuffer buffer = bufferSupplier.get();
//		System.out.println("buffer position: "+buffer.position());
//		System.out.println("writing mark: "+mark);
		buffer.put((byte)mark.ordinal());
	
//		System.out.println("writing key of length: "+key.length);
		buffer.putInt(key.length);
		buffer.put(key);
		
		buffer.putInt(value.length);
		buffer.put(value);
		
		return buffer;
	}

	static Record readExternal(Supplier<ByteBuffer> bufferSupplier) throws IOException {
		
		ByteBuffer buffer = bufferSupplier.get();
		
		byte mark = buffer.get();
		
		if(mark==Record.Mark.EMPTY.ordinal()) {
			return null;
		}
		
		Record.Mark markEnum = Record.Mark.PRESENT;
		
		if(mark==2) {
			markEnum = Record.Mark.REMOVED;			
		}
		
		if(mark<0 || mark > 2) {
			throw new RuntimeException("invalid mark");
		}
		
		int keyLength = buffer.getInt();
				
		byte[] key = new byte[keyLength];
		buffer.get(key);
		
		int valueLength = buffer.getInt();
		byte[] value= new byte[valueLength];
		buffer.get(value);
		
		return new Record(key,value,markEnum);
	}

	static Record of(Entry tuple) throws IOException {
		var keyByteArrayOutput = new ByteArrayOutputStream();
		try(var objectOutput = new ObjectOutputStream(keyByteArrayOutput)){
			objectOutput.writeObject(tuple.key());
		}
		
		var valueByteArrayOutput = new ByteArrayOutputStream();
		try(var objectOutput = new ObjectOutputStream(valueByteArrayOutput)){
			objectOutput.writeObject(tuple.value());
		}
		
		return new Record(keyByteArrayOutput.toByteArray(), valueByteArrayOutput.toByteArray());
	}

}
