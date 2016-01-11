package pl.symentis.bytecode.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;

public class MemoizeMethodTransformer implements ClassFileTransformer {

	private final ClassPool classPool;
	private final MemoizeMethodGenerator generator;

	MemoizeMethodTransformer(ClassPool classPool,String annotationClassname) {
		this.classPool = classPool;
		this.generator = new MemoizeMethodGenerator(classPool,annotationClassname);
	}

	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		classPool.appendClassPath(new ByteArrayClassPath(className, classfileBuffer));

		try {
			CtClass ctClass = classPool.get(className);

			if (generator.hasMemoizedMethods(ctClass)) {
				CtClass transformedClass = generator.generateMemoizedMethods(ctClass);
				return transformedClass.toBytecode();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}