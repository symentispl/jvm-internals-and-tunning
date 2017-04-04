package pl.symentis.jvminternals.bytecode;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;
import static pl.symentis.jvminternals.bytecode.ClassFileHelper.writeClassToFile;

import java.io.PrintStream;
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
	
	public static void main(String[] args) throws Exception {

		String classname = "ClassWithInvokeDynamic";

		ClassWriter writer = new ClassWriter(0);

		writer.visit(
				V1_8,
				ACC_PUBLIC,
				classname,
				null,
				"java/lang/Object",
				new String[] { getInternalName(Runnable.class) });

		MethodVisitor constructor = writer.visitMethod(
				ACC_PUBLIC,
				"<init>", getMethodDescriptor(VOID_TYPE),
				null, 
				null);

		// default constructor code
		constructor.visitCode();
		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitMethodInsn(
				INVOKESPECIAL, 
				"java/lang/Object",
				"<init>", 
				getMethodDescriptor(Type.VOID_TYPE),
				false);
		constructor.visitInsn(RETURN);
		constructor.visitMaxs(1, 1);
		constructor.visitEnd();

		// implementation of interface Runnable method run
		MethodVisitor method = writer.visitMethod(
				ACC_PUBLIC,
				"run", 
				getMethodDescriptor(VOID_TYPE), 
				null, 
				null);
		method.visitCode();

		// define invokedynamic bootstrap
		MethodType methodType = MethodType.methodType(CallSite.class,MethodHandles.Lookup.class, String.class, MethodType.class);

		Handle bootstrap = new Handle(
				H_INVOKESTATIC, 
				getInternalName(GenerateClassWithInvokeDynamic.class),
				"bootstrap", 
				methodType.toMethodDescriptorString(),
				false);

		method.visitLdcInsn(1);
		method.visitLdcInsn(1);

		// first call site where argument are int's
		method.visitInvokeDynamicInsn(
				"callMe", 
				getMethodDescriptor(getType(String.class), Type.INT_TYPE, Type.INT_TYPE),
				bootstrap);

		// print result of invoke dynamic method call
		method.visitVarInsn(ASTORE,1);
		method.visitFieldInsn(GETSTATIC, getInternalName(System.class), "out", getDescriptor(PrintStream.class));
		method.visitVarInsn(ALOAD,1);
		method.visitMethodInsn(
				INVOKEVIRTUAL, 
				getInternalName(PrintStream.class), 
				"println", 
				getMethodDescriptor(VOID_TYPE, getType(String.class)),
				false);
		
		
		method.visitLdcInsn("Hello ");
		method.visitLdcInsn(" world!");

		// second call site where arguments are String's
		method.visitInvokeDynamicInsn(
				"callMe", 
				getMethodDescriptor(getType(String.class), getType(String.class), getType(String.class)),
				bootstrap);

		// print result of invoke dynamic method call
		method.visitVarInsn(Opcodes.ASTORE,1);
		method.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(System.class), "out", Type.getDescriptor(PrintStream.class));
		method.visitVarInsn(Opcodes.ALOAD,1);
		method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(PrintStream.class), "println", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)),false);

		method.visitInsn(Opcodes.RETURN);

		method.visitMaxs(2, 3);
		method.visitEnd();

		writer.visitEnd();

		byte[] classBuff = writer.toByteArray();

		writeClassToFile(classname, classBuff);

		Class<?> class1 = new DefiningClassLoader().defineClass(classname,
				classBuff);
		Runnable object = (Runnable) class1.newInstance();
		
		for(int i=0;i<3;i++){
			System.out.println("--> calling run method");
			object.run();			
		}

	}

	// invoke dynamic call site bootstrap method
	public static CallSite bootstrap(MethodHandles.Lookup caller, String name,
			MethodType type) throws NoSuchMethodException,
			IllegalAccessException {
		
		System.out.println(".. and now linking call site of method type "+type);

		MethodHandle methodHandle = caller.findStatic(GenerateClassWithInvokeDynamic.class, name, type);
		
		return new ConstantCallSite(methodHandle);
		
	}

	public static String callMe(int i, int j){
		System.out.println("calling me with ints");
		return Integer.toString(i+j);
	}
	
	public static String callMe(String i, String j){
		System.out.println("calling me with String");
		return i+j;
	}

}
