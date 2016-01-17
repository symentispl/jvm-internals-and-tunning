package pl.symentis.bytecode.jitescript;

import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;
import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.JDKVersion;
import me.qmx.jitescript.JiteClass;

public class GenerateClass {

	public static void main(String[] args) throws Exception {
		JiteClass jclass = new JiteClass("JiteGeneratedClass",
				new String[] { p(IntComparator.class) });
		jclass.defineDefaultConstructor();

		String methodName = "compare";
		int modifiers = JiteClass.ACC_PUBLIC;
		String signature = sig(Integer.TYPE, new Class[] { Integer.TYPE,
				Integer.TYPE });

		CodeBlock methodBody = new CodeBlock();
		methodBody.ldc(0);
		methodBody.ireturn();

		jclass.defineMethod(methodName, modifiers, signature, methodBody);

		byte[] bytes = jclass.toBytes(JDKVersion.V1_8);

		DefiningClassLoader loader = new DefiningClassLoader();
		Class<?> clazz = loader.defineClass("JiteGeneratedClass", bytes);
		IntComparator instance = (IntComparator) clazz.newInstance();
		System.out.println(instance.compare(1, 2));

	}
}
