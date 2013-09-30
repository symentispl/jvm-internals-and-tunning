package pl.symentis.jvminternals.bytecode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Type.getMethodType;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class GenerateClassWithConditionals {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

		writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "MyClass", null, "java/lang/Object", new String[] { Type
				.getType(Comparator.class).getInternalName() });

		MethodVisitor constructor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", getMethodType(Type.VOID_TYPE)
				.getInternalName(), null, null);

		constructor.visitCode();
		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", getMethodType(Type.VOID_TYPE)
				.getInternalName());
		constructor.visitInsn(Opcodes.RETURN);
		constructor.visitMaxs(1, 1);
		constructor.visitEnd();

		MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC, "greaterThan",
				getMethodType(Type.BOOLEAN_TYPE, Type.INT_TYPE, Type.INT_TYPE).getInternalName(), null, null);
		method.visitCode();

		method.visitVarInsn(Opcodes.ILOAD, 1);
		method.visitVarInsn(Opcodes.ILOAD, 2);
		Label label = new Label();
		method.visitJumpInsn(Opcodes.IF_ICMPEQ, label);
		method.visitLdcInsn(0);
		method.visitInsn(Opcodes.IRETURN);
		
		method.visitLabel(label);
		method.visitLdcInsn(1);
		method.visitInsn(Opcodes.IRETURN);
		
		method.visitMaxs(1, 1);
		method.visitEnd();

		writer.visitEnd();

		byte[] classBuff = writer.toByteArray();

		writeClassToFile(classBuff);

		Class<?> class1 = new DefiningClassLoader().defineClass("MyClass", classBuff);
		Comparator object = (Comparator) class1.newInstance();
		System.out.println(object.greaterThan(2, 2));

	}

	private static void writeClassToFile(byte[] classBuff) throws FileNotFoundException, IOException {
		FileOutputStream fileWriter = new FileOutputStream("MyClass.class");
		fileWriter.write(classBuff);
		fileWriter.close();
	}

	public String greaterThan(int i, int y) {
		if (i == y) {
			return "same";
		}
		if (i > y) {
			return "greater";
		}
		return "smaller";
	}

}
