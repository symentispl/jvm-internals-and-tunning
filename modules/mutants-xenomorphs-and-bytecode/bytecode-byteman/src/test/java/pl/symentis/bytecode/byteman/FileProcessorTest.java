package pl.symentis.bytecode.byteman;

import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * This is example of fault injection testing with Byteman
 *
 */
@RunWith(BMUnitRunner.class)
@Ignore( "cannot attach to JVM under JDK 11")
public class FileProcessorTest {

	@Rule
	public ExpectedException expectedException =  ExpectedException.none(); 
	
	@BMRule(
		name = "throw security exception", 
		targetClass = "FileInputStream", 
		targetMethod = "<init>(File)", 
		condition = "$1.getName().equals(\"badname.txt\")", 
		action = "throw new FileNotFoundException()")
	@Test
	public void testSecurityException() throws Exception {
		FileProcessor fileProcessor = new FileProcessor();
		
		expectedException.expect(IllegalArgumentException.class);
		
		fileProcessor.processFile("badname.txt");
	}

}
