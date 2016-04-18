package pl.symentis.bytecode.byteman;

import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(BMUnitRunner.class)
public class FileProcessorTest {

	@Rule
	public ExpectedException expectedException =  ExpectedException.none(); 
	
	@BMRule(
		name = "throw security exception", 
		targetClass = "FileInputStream", 
		targetMethod = "<init>(File)", 
		condition = "$1.getName().equals(\"badname.txt\")", 
		action = "throw new NullPointerException(\"bad name!\")")
	@Test
	public void testSecurityException() throws Exception {
		FileProcessor myTestObj = new FileProcessor();
		
		expectedException.expect(NullPointerException.class);
		
		myTestObj.processFile("badname1.txt");
	}

}
