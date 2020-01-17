package introdb.pagecache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import introdb.fs.Block;

@FunctionalInterface
public interface BlockLoader {

	Block read(int blockNumber, Supplier<ByteBuffer> byteBufferSupplier) throws IOException;

}
