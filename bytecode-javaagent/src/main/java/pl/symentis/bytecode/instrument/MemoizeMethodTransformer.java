package pl.symentis.bytecode.instrument;

import static java.lang.System.out;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

public class MemoizeMethodTransformer implements ClassFileTransformer {

	private final ClassPool classPool;
	private final MemoizeMethodGenerator generator;

	public MemoizeMethodTransformer(ClassPool classPool, String annotationClassname) {
		this.classPool = classPool;
		// make sure javassist will see classes loaded before this code
		classPool.appendClassPath(new LoaderClassPath(MemoizeMethodTransformer.class.getClassLoader()));
		this.generator = new MemoizeMethodGenerator(classPool, annotationClassname);
	}

	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {


		try {
			// classname uses / notation, and javassist expects dots
			String dottedClassname = className.replaceAll("/", ".");
			// transform only our code, just for loading speed up :)
			if (dottedClassname.startsWith("pl.symentis")) {
				//make sure classpool knows the class we are going to check and transform
				classPool.appendClassPath(new ByteArrayClassPath(dottedClassname, classfileBuffer));
				out.println("trying to transform: "+dottedClassname);
				CtClass ctClass = classPool.get(dottedClassname);
				if (generator.hasMemoizedMethods(ctClass)) {
					out.println("has memoized methods");
					CtClass transformedClass = generator.generateMemoizedMethods(ctClass);
					return transformedClass.toBytecode();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}