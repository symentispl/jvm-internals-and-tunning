package pl.symentis.jvminternals.bytecode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Type.getMethodType;
import static org.objectweb.asm.Type.getType;
import static pl.symentis.jvminternals.bytecode.ClassFileHelper.writeClassToFile;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Example showing JVM jump opcodes
 * 
 * @author jaroslaw.palka@symentis.pl
 * 
 */
public class GenerateClassWithConditionals {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String classname = "ClassWithConditional";

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES
				| ClassWriter.COMPUTE_MAXS);

		writer.visit(
				Opcodes.V1_6,
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
				getMethodType(getType(String.class), Type.INT_TYPE,
						Type.INT_TYPE).getInternalName(), null, null);
		method.visitCode();

		method.visitVarInsn(Opcodes.ILOAD, 1);
		method.visitVarInsn(Opcodes.ILOAD, 2);
		Label label = new Label();
		method.visitJumpInsn(Opcodes.IF_ICMPEQ, label);
		method.visitLdcInsn("greater");
		method.visitInsn(Opcodes.ARETURN);

		method.visitLabel(label);
		method.visitLdcInsn("smaller or equal");
		method.visitInsn(Opcodes.ARETURN);

		method.visitMaxs(1, 1);
		method.visitEnd();

		writer.visitEnd();

		byte[] classBuff = writer.toByteArray();

		writeClassToFile(classname, classBuff);

		Class<?> class1 = new DefiningClassLoader().defineClass(classname,
				classBuff);
		Comparator object = (Comparator) class1.newInstance();
		System.out.println(object.greaterThan(2, 1));

	}

}
