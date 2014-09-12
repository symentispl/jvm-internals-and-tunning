package pl.symentis.bytecode.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class Agent {

	public static void premain(String agentArgs, Instrumentation inst) {

		final ClassPool classPool = new ClassPool();
		inst.addTransformer(new ClassFileTransformer() {

			public byte[] transform(ClassLoader loader, String className,
					Class classBeingRedefined,
					ProtectionDomain protectionDomain, byte[] classfileBuffer)
					throws IllegalClassFormatException {

				classPool.appendClassPath(new ByteArrayClassPath(className,
						classfileBuffer));
				System.out.println(">>>" + className);
				try {
					CtClass ctClass = classPool.get(className);
					System.out.println(">>>" + ctClass);

				} catch (NotFoundException e) {
					e.printStackTrace();
				}

				return null;
			}
		});

	}

}
