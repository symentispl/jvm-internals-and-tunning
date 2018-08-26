
import static java.lang.String.format;
import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Get ready with command:
 * 	dd if=/dev/urandom of=geecon.json bs=512 count=1024
 * @author jaroslaw.palka@symentis.pl
 *
 */
public class BasicIO {

	public static void main(String[] args) throws IOException {

		File file = new File("geecon.json");

		try (InputStream input = new FileInputStream(file)) {

			byte[] buff = new byte[512];
			int fileSize = 0, buffSize;
			while ((buffSize = input.read(buff)) != -1) {
				fileSize += buffSize;
			}
			out.println(format("file size is = %d", fileSize));

		}
	}

}
