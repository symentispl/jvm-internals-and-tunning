package pl.symentis.jvminternals.bytecode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getMethodType;
import static pl.symentis.jvminternals.bytecode.ClassFileHelper.writeClassToFile;

import java.util.ArrayList;

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
public class GenerateClassWithPrivateField {

	public static void main(String[] args) throws Exception {
		String classname = "ClassWithPrivateField";

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES
				| ClassWriter.COMPUTE_MAXS);

		writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, classname, null,
				"java/lang/Object", new String[] {});

		writer.visitField(Opcodes.ACC_PRIVATE, "list",
				Type.getDescriptor(ArrayList.class), null, null);

		MethodVisitor constructor = writer.visitMethod(Opcodes.ACC_PUBLIC,
				"<init>", getMethodType(Type.VOID_TYPE, Type.INT_TYPE)
						.getDescriptor(), null, null);

		constructor.visitCode();
		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object",
				"<init>", getMethodType(Type.VOID_TYPE).getDescriptor(),false);

		constructor.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");

		// prove that compiler can also play part in generating optimal
		// bytecode, Opcode.DUP :)

		constructor.visitVarInsn(ASTORE, 2);
		constructor.visitVarInsn(ALOAD, 2);
		constructor.visitVarInsn(Opcodes.ILOAD, 1);
		constructor.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList",
				"<init>", getMethodType(Type.VOID_TYPE, Type.INT_TYPE)
						.getDescriptor(),false);

		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitVarInsn(ALOAD, 2);

		constructor.visitFieldInsn(Opcodes.PUTFIELD, classname, "list",
				Type.getDescriptor(ArrayList.class));

		constructor.visitInsn(RETURN);
		constructor.visitMaxs(1, 1);
		constructor.visitEnd();

		writer.visitEnd();

		byte[] classBuff = writer.toByteArray();

		writeClassToFile(classname, classBuff);

		Class<?> klass = new DefiningClassLoader().defineClass(classname,
				classBuff);
		klass.getConstructor(Integer.TYPE).newInstance(5);

	}

}
