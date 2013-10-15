package pl.symentis.jvminternals.bytecode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Type.getMethodType;
import static pl.symentis.jvminternals.bytecode.ClassFileHelper.writeClassToFile;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A basic example how invokedynamic works.
 * 
 * @author jaroslaw.palka@symentis.pl
 * 
 */
public class GenerateClassWithInvokeDynamic {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String classname = "ClassWithInvokeDynamic";

		ClassWriter writer = new ClassWriter(0);

		writer.visit(
				Opcodes.V1_7,
				Opcodes.ACC_PUBLIC,
				classname,
				null,
				"java/lang/Object",
				new String[] { Type.getType(Comparator.class).getInternalName() });

		MethodVisitor constructor = writer.visitMethod(Opcodes.ACC_PUBLIC,
				"<init>", getMethodType(Type.VOID_TYPE).getInternalName(),
				null, null);

		constructor.visitCode();
		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object",
				"<init>", getMethodType(Type.VOID_TYPE).getInternalName());
		constructor.visitInsn(Opcodes.RETURN);
		constructor.visitMaxs(1, 1);
		constructor.visitEnd();

		MethodVisitor method = writer.visitMethod(
				Opcodes.ACC_PUBLIC,
				"greaterThan",
				getMethodType(Type.getType(String.class), Type.INT_TYPE,
						Type.INT_TYPE).getInternalName(), null, null);
		method.visitCode();

		MethodType methodType = MethodType.methodType(CallSite.class,
				MethodHandles.Lookup.class, String.class, MethodType.class);

		Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC, Type.getType(
				GenerateClassWithInvokeDynamic.class).getInternalName(),
				"bootstrap", methodType.toMethodDescriptorString());

		method.visitVarInsn(Opcodes.ILOAD, 1);
		method.visitVarInsn(Opcodes.ILOAD, 2);

		method.visitInvokeDynamicInsn("callMe", Type.getMethodDescriptor(
				Type.getType(String.class), Type.INT_TYPE, Type.INT_TYPE),
				bootstrap);

		method.visitInsn(Opcodes.ARETURN);

		method.visitMaxs(2, 3);
		method.visitEnd();

		writer.visitEnd();

		byte[] classBuff = writer.toByteArray();

		writeClassToFile(classname, classBuff);

		Class<?> class1 = new DefiningClassLoader().defineClass(classname,
				classBuff);
		Comparator object = (Comparator) class1.newInstance();
		System.out.println(object.greaterThan(2, 2));

	}

	// invoke dynamic call site bootstrap method
	public static CallSite bootstrap(MethodHandles.Lookup caller, String name,
			MethodType type) throws NoSuchMethodException,
			IllegalAccessException {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		Class<?> thisClass = lookup.lookupClass();
		MethodHandle methodHandle = lookup
				.findStatic(thisClass, "alwaysTrue", MethodType.methodType(
						String.class, Integer.TYPE, Integer.TYPE));
		return new ConstantCallSite(methodHandle.asType(type));
	}

	public static String alwaysTrue(int x, int y) {
		return "true";
	}

	public static String realMethod(int x, int y) {
		if (x > y) {
			return "true";
		} else {
			return "false";
		}
	}
}
