package introdb.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

public interface BlockFile extends AutoCloseable{

	int blockSize();

	Block create(int blockNymber, int blockSize);

	void write(int blockNumber, ByteBuffer byteBuffer) throws IOException;

	Block read(int blockNumber, Supplier<ByteBuffer> byteBufferSupplier) throws IOException;

}
