package introdb.fs;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * 
 * @author jaroslaw.palka@symentis.pl
 *
 */
public class FileChannelBlockFile implements BlockFile {

	private static final Logger LOGGER = Logger.getLogger(FileChannelBlockFile.class.getName());

	private final FileChannel fileChannel;
	private final int blockSize;

	public FileChannelBlockFile(FileChannel fileChannel, int blockSize) {
		super();
		this.fileChannel = fileChannel;
		this.blockSize = blockSize;
	}

	/**
	 * 
	 * @param blockNumber
	 * @param byteBufferSupplier
	 * @return
	 * @throws IOException
	 */
	@Override
	public Block read(int blockNumber, Supplier<ByteBuffer> byteBufferSupplier) throws IOException {

		LOGGER.fine(() -> format("reading block %d from file %s", blockNumber, fileChannel));

		if (blockNumber < 0) {
			throw new IllegalArgumentException("negative block number");
		}

		var byteBuffer = byteBufferSupplier.get();

		if (byteBuffer.remaining() == blockSize) {
			var readBytesCount = fileChannel.read(byteBuffer, blockNumber * blockSize);
			byteBuffer.rewind();
			return ByteBufferBlock.read(blockNumber, blockSize, byteBuffer);
		} else {
			throw new IllegalStateException(
					format("buffer %s doesn't have enough bytes, expected $d", byteBuffer, blockSize));
		}
	}

	@Override
	public void write(int blockNumber, ByteBuffer byteBuffer) throws IOException {
		LOGGER.fine(() -> format("writing block %d (buffer %s) to file %s", blockNumber, byteBuffer, fileChannel));
		if (blockNumber < 0) {
			throw new IllegalArgumentException("negative block number");
		}

		if (byteBuffer.remaining() == blockSize) {
			fileChannel.write(byteBuffer, blockNumber * blockSize);
		} else {
			throw new IllegalArgumentException(
					format("buffer %s doesn't have enough bytes, expected $d", byteBuffer, blockSize));
		}
	}

	@Override
	public Block create(int blockNymber, int blockSize) {
		return ByteBufferBlock.create(blockNymber, blockSize);
	}

	@Override
	public void close() throws Exception {

	}

	@Override
	public int blockSize() {
		return blockSize;
	}

}
