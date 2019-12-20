package pl.symentis.jvminternals.bytecode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClassFileHelper {

	public static void writeClassToFile(String classFilename, byte[] classBuff)
			throws FileNotFoundException, IOException {
		FileOutputStream fileWriter = new FileOutputStream(classFilename
				+ ".class");
		try {
			fileWriter.write(classBuff);
		} finally {
			fileWriter.close();
		}
	}
}
