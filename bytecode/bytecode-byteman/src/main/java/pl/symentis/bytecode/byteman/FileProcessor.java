package pl.symentis.bytecode.byteman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class FileProcessor {

	public List<String> processFile(String string) throws IOException {

		try {
			try (FileInputStream inputStream = new FileInputStream(new File(string))) {
				return IOUtils.readLines(inputStream);
			}
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
