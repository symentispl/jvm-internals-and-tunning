package pl.symentis.bytecode.byteman;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.WithByteman;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * This is example of fault injection testing with Byteman
 *
 */
@WithByteman
@Disabled("failes under JDK 11")
public class FileProcessorTest {

	@BMRule(
		name = "throw security exception", 
		targetClass = "FileInputStream", 
		targetMethod = "<init>(File)", 
		condition = "$1.getName().equals(\"badname.txt\")", 
		action = "throw new FileNotFoundException()")
	@Test
	public void testSecurityException() throws Exception {
		FileProcessor fileProcessor = new FileProcessor();
		
		assertThrows(IllegalArgumentException.class, 
					 () -> fileProcessor.processFile("badname.txt"));

	}

}
