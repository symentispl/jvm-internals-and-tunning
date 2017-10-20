package pl.symentis.jvm.example4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.BiFunction;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoPool;

class Journal {

	private static final int PAGE_SIZE = 4 * 1024/* kB, default Linux page size */;

	private static final int JOURNAL_SIZE = 10 * 1024 * 1024/* 10 MBs */;
	
	private final KryoPool kryoPool;
	private final FileChannel fileChannel;
	private final MappedByteBuffer mappedByteBuffer;

	private volatile int position;

	Journal(Path baseDir) throws IOException {
		kryoPool = new KryoPool.Builder(Kryo::new).build();

		Path filePath = baseDir.resolve(Paths.get("00000001"));

		fileChannel = FileChannel.open(filePath, 
				StandardOpenOption.CREATE, 
				StandardOpenOption.READ,
				StandardOpenOption.WRITE);

		mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, JOURNAL_SIZE);
	}

	/**
	 * Append new record to journal, once we reach end of file, rewind buffer and
	 * overwrite old entries
	 * 
	 * <strong>This methods is not thread safe</strong>
	 * 
	 * @param record
	 * @return
	 * @throws IOException
	 */
	Location append(Record record) throws IOException {
		// serialize record, and check if we have enough space
		try (ByteBufferOutput output = kryoPool.run(serialize(record))) {
			ByteBuffer buffer = output.getByteBuffer();
			buffer.flip();
			if (mappedByteBuffer.remaining() < buffer.limit()) {
				mappedByteBuffer.rewind();
			}
			mappedByteBuffer.put(buffer);
			position = mappedByteBuffer.position();
		}

		return null;
	}


	/**
	 * this method applies fold function on all recored in journal from oldest to
	 * newest
	 * 
	 * @param function
	 * @return
	 * @throws IOException
	 */
	Integer fold(BiFunction<Integer, Record, Integer> function) throws IOException {
		int currentPosition = position;
		MappedByteBuffer readOnlymappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, 0, JOURNAL_SIZE);
		readOnlymappedByteBuffer.limit(currentPosition);

		ByteBufferInput input = new ByteBufferInput(readOnlymappedByteBuffer);
		Integer folded = 0;
		while (wasNotBufferRewinded(currentPosition) && readOnlymappedByteBuffer.hasRemaining()) {
			folded = function.apply(folded, deserialize(input));
		}

		return folded;
	}

	private static KryoCallback<ByteBufferOutput> serialize(Record record) {
		return k -> {
			ByteBufferOutput output = new ByteBufferOutput(PAGE_SIZE);
			k.writeObject(output, record);
			return output;
		};
	}

	private Record deserialize(ByteBufferInput input) {
		return kryoPool.run(k -> k.readObject(input, Record.class));
	}

	private boolean wasNotBufferRewinded(int currentPosition) {
		return currentPosition <= position;
	}

}
