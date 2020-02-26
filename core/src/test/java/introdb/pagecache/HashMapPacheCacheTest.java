package introdb.pagecache;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import introdb.fs.Block;
import introdb.fs.FileChannelBlockFile;

public class HashMapPacheCacheTest {

	
	@Test
	public void load_page(@TempDir Path directory) throws Exception {
		// given
		FileChannel fileChannel = FileChannel.open(directory.resolve("heap.001"), CREATE_NEW,READ,WRITE);
		var blockFile = new FileChannelBlockFile( fileChannel, 4*1024);
		var pageCache = new HashMapPageCache(blockFile,1024,0.2f);

		int blockSize = 4096;
		var array = new byte[blockSize];
		Arrays.fill(array, (byte)1);
		
		var loader = mock(BlockLoader.class);
		when(loader.read(eq(0), any())).then(
				invocation -> {
					int blockNr = invocation.getArgument(0, Integer.class);
					ByteBuffer byteBuffer = (ByteBuffer) invocation.getArgument(1, Supplier.class).get();
					byteBuffer.put(array);
					byteBuffer.rewind();
					return Block.byteBufferBlock(blockNr,byteBuffer);
				});
		// when
		var page = pageCache.getPage(0, () -> ByteBuffer.allocate(blockSize), loader);
		// then	
		assertThat(page.dirty()).isFalse();
		assertThat(page.block().array()).isEqualTo(array);		
		assertThat(page.block().position()).isEqualTo(0);
		verify(loader).read(eq(0), any());
		// when
		page = pageCache.getPage(0, () -> ByteBuffer.allocate(blockSize), loader);
		// then
		assertThat(page.dirty()).isFalse();
		assertThat(page.block().array()).isEqualTo(array);		
		assertThat(page.block().position()).isEqualTo(0);
		verifyNoMoreInteractions(loader);
	}

}
