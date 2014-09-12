package pl.symentis.jvminternals.bytecode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getMethodType;
import static org.objectweb.asm.Type.getType;
import static pl.symentis.jvminternals.bytecode.ClassFileHelper.writeClassToFile;

import java.io.PrintStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Simple example how generate new class, define constructor, call virtual
 * methods and access static fields.
 * 
 * @author jaroslaw.palka@symentis.pl
 */
public class GenerateClassWithDefaultConstructor {

	public static void main(String[] args) throws Exception {
		String classname = "ClassWithDefaultConstructor";

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES
				| ClassWriter.COMPUTE_MAXS);

		writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, classname, null,
				"java/lang/Object", new String[] {});

		MethodVisitor constructor = writer.visitMethod(Opcodes.ACC_PUBLIC,
				"<init>",
				getMethodType(Type.VOID_TYPE, Type.getType(Integer.class))
						.getInternalName(), null, null);

		constructor.visitCode();
		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object",
				"<init>", getMethodType(Type.VOID_TYPE).getInternalName(),false);

		constructor.visitVarInsn(ALOAD, 1);
		constructor.visitVarInsn(ASTORE, 2);
		constructor.visitVarInsn(ALOAD, 2);

		constructor.visitMethodInsn(INVOKEVIRTUAL, getType(Integer.class)
				.getInternalName(), "toString",
				getMethodDescriptor(getType(String.class)),false);
		constructor.visitVarInsn(ASTORE, 3);

		String desc = getType(PrintStream.class).getDescriptor();
		constructor.visitFieldInsn(GETSTATIC, getType(System.class)
				.getInternalName(), "out", desc);
		constructor.visitVarInsn(ALOAD, 3);

		constructor.visitMethodInsn(INVOKEVIRTUAL, getType(PrintStream.class)
				.getInternalName(), "println",
				getMethodDescriptor(Type.VOID_TYPE, getType(String.class)),false);

		constructor.visitInsn(RETURN);
		constructor.visitMaxs(1, 1);
		constructor.visitEnd();

		writer.visitEnd();

		byte[] classBuff = writer.toByteArray();

		writeClassToFile(classname, classBuff);

		Class<?> klass = new DefiningClassLoader().defineClass(classname,
				classBuff);
		klass.getConstructor(Integer.class).newInstance(5);

	}
}
