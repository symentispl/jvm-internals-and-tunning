package pl.symentis.jvminternals.bytecode;

/**
 * 
 * @author jaroslaw.palka@symentis.pl
 * 
 */
public class DefiningClassLoader extends ClassLoader {

	public Class<?> defineClass(String name, byte[] bytes) {
		return defineClass(name, bytes, 0, bytes.length);
	}

}
