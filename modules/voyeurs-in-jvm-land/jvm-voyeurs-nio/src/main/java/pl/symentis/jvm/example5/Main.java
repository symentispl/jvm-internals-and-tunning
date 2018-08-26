package pl.symentis.jvm.example5;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Paths.get;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.control.Try;

public class Main {

	private static final int FS_WATCH_TIMEOUT_MS = 5;

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static final byte[] BUFFER = new byte[512 * 1024];

	private static final RandomStringGenerator RANDOM_STRING_GEN = new RandomStringGenerator.Builder().withinRange('a', 'z').build();

	private static ScheduledExecutorService scheduledExecutorService;

	private static Path inputPath;

	private static ExecutorService ioExecutorService;

	private static Path outputPath;

	public static void main(String[] args) throws IOException {

		ioExecutorService = Executors.newCachedThreadPool();

		scheduledExecutorService = Executors.newScheduledThreadPool(2);

		inputPath = Files.createTempDirectory("example5i").toAbsolutePath();

		outputPath = Files.createTempDirectory("example5o").toAbsolutePath();

		WatchService watcher = FileSystems.getDefault().newWatchService();

		WatchKey watchKey = inputPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);

		// watch for new files
		scheduledExecutorService.schedule(watchNewFiles(watchKey, new ArrayList<Path>()), FS_WATCH_TIMEOUT_MS,
				TimeUnit.MILLISECONDS);

		// generate files
		scheduledExecutorService.scheduleAtFixedRate(newFiles(inputPath), 1000, 10, TimeUnit.MILLISECONDS);

	}

	private static void scheduleWatchNewFiles(WatchKey watchKey, ArrayList<Path> files) {
		scheduledExecutorService.schedule(watchNewFiles(watchKey, files), FS_WATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
	}

	private static void triggerMergeOfFiles(List<Path> files) {
		ioExecutorService.execute(() -> mergeFiles(files));
	}

	private static Runnable watchNewFiles(WatchKey watchKey, ArrayList<Path> files) {

		return () -> {

			LOGGER.debug("watching for changes in directory {}", inputPath);

			if (files.size() > 10) {
				triggerMergeOfFiles(new ArrayList<>(files));
				files.clear();
			}

			List<Path> newPaths = watchKey
					.pollEvents()
					.stream()
					.map(WatchEvent::context)
					.map(Path.class::cast)
					.collect(toList());

			files.addAll(newPaths);

			scheduleWatchNewFiles(watchKey, files);
		};
	}

	private static void mergeFiles(List<Path> files) {

		LOGGER.debug("merging files {}", files);

		List<CompletableFuture<ByteBuffer>> ioTasks = files
				.stream()
				.map(inputPath::resolve)
				.map(path -> supplyAsync(() -> readByteBuffer(path), ioExecutorService))
				.collect(toList());

		allOf(asTasks(ioTasks))
			.thenRunAsync(() -> readBuffersAndWrite(ioTasks), ioExecutorService)
			.thenRunAsync(() -> triggerDeleteOfMergedFiles(files), ioExecutorService);

	}

	private static CompletableFuture<?>[] asTasks(List<CompletableFuture<ByteBuffer>> ioTasks) {
		return ioTasks.toArray(new CompletableFuture<?>[ioTasks.size()]);
	}

	private static void readBuffersAndWrite(List<CompletableFuture<ByteBuffer>> ioTasks) {
		List<Buffer> buffers = ioTasks
				.stream()
				.map(buffer -> Try.of(() -> buffer.get()))
				.map(Try::get)
				.map(ByteBuffer::flip)
				.map(ByteBuffer.class::cast).collect(toList());

		Path filename = outputPath.resolve(Paths.get(RANDOM_STRING_GEN.generate(32)));

		LOGGER.debug("merging files into {}", filename);

		try (FileChannel file = FileChannel.open(filename, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
			file.write(buffers.toArray(new ByteBuffer[] {}));
		} catch (IOException e) {
			LOGGER.error("cannot write merged buffers", e);
		}
	}

	private static ByteBuffer readByteBuffer(Path path) {
		try (FileChannel file = FileChannel.open(path, StandardOpenOption.READ)) {
			long size = file.size();
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) size);
			int readSize = file.read(byteBuffer);
			LOGGER.debug("read bytes {} from file {}", readSize, path);
			return byteBuffer;
		} catch (IOException e) {
			LOGGER.error("cannot read byte buffer from file {}", path, e);
			throw new RuntimeException("cannot read byte buffer from file", e);
		}
	}

	private static void triggerDeleteOfMergedFiles(List<Path> files) {
		files
			.stream()
			.map(inputPath::resolve)
			.map(Main::deleteFile)
			.forEach(ioExecutorService::execute);
	}

	private static Runnable deleteFile(Path path) {
		return () -> {
			LOGGER.debug("removing files {}", path);
			try {
				Files.delete(path);
			} catch (IOException e) {
				LOGGER.error("cannot remove file {}", path);
			}
		};
	}

	private static Runnable newFiles(Path inputPath) {
		return () -> {
			try (OutputStream out = newOutputStream(createFile(inputPath.resolve(get(RANDOM_STRING_GEN.generate(8)))), StandardOpenOption.APPEND)) {
				out.write(BUFFER);
			} catch (IOException e) {
				LOGGER.error("cannot write new files in {}", inputPath, e);
			}
		};
	}

}
