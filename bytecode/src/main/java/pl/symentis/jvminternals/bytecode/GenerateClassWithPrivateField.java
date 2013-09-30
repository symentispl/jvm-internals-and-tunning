package pl.symentis.jvminternals.bytecode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getMethodType;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
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
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES
				| ClassWriter.COMPUTE_MAXS);

		writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "MyClass", null,
				"java/lang/Object", new String[] {});

		FieldVisitor field = writer.visitField(Opcodes.ACC_PRIVATE, "list",
				Type.getDescriptor(ArrayList.class), null, null);

		MethodVisitor constructor = writer.visitMethod(Opcodes.ACC_PUBLIC,
				"<init>", getMethodType(Type.VOID_TYPE, Type.INT_TYPE)
						.getDescriptor(), null, null);

		constructor.visitCode();
		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object",
				"<init>", getMethodType(Type.VOID_TYPE).getDescriptor());

		constructor.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");

		//proove that compiler can also play part in generating optimal bytecode, Opcode.DUP :)
		
		constructor.visitVarInsn(ASTORE, 2);
		constructor.visitVarInsn(ALOAD, 2);
		constructor.visitVarInsn(Opcodes.ILOAD, 1);
		constructor.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList",
				"<init>", getMethodType(Type.VOID_TYPE, Type.INT_TYPE)
						.getDescriptor());

		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitVarInsn(ALOAD, 2);

		constructor.visitFieldInsn(Opcodes.PUTFIELD, "MyClass", "list",
				Type.getDescriptor(ArrayList.class));

		constructor.visitInsn(RETURN);
		constructor.visitMaxs(1, 1);
		constructor.visitEnd();

		writer.visitEnd();

		byte[] classBuff = writer.toByteArray();

		writeClassToFile(classBuff);

		Class<?> class1 = new DefiningClassLoader().defineClass("MyClass",
				classBuff);
		Object object = class1.getConstructor(Integer.TYPE).newInstance(5);

	}

	private static void writeClassToFile(byte[] classBuff)
			throws FileNotFoundException, IOException {
		FileOutputStream fileWriter = new FileOutputStream("MyClass.class");
		fileWriter.write(classBuff);
		fileWriter.close();
	}

	private ArrayList list;

	public GenerateClassWithPrivateField(int i) {
		list = new ArrayList(i);
	}
}
