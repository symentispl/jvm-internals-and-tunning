package pl.symentis.bytecode.instrument;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javassist.ClassPool;
import javassist.CtClass;

public class AgentTest {

	@Test
	@Disabled
	public void generate_memoized_method() throws Exception {

		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.getCtClass("pl.symentis.bytecode.instrument.TestClass");

		MemoizeMethodGenerator transformer = new MemoizeMethodGenerator(classPool,"pl.symentis.bytecode.instrument.TestAnnotation");

		CtClass ctTransfromedClass = transformer.generateMemoizedMethods(ctClass);

		@SuppressWarnings("deprecation")
		Class<?> clazz = ctTransfromedClass.toClass(AgentTest.class.getClassLoader());

		Field field = clazz.getField("_memoizeCache");
		Map<?, ?> cache = (Map<?, ?>) field.get(clazz);

		TestInterface memoized = (TestInterface) clazz.newInstance();

		Integer one = new Integer(1);
		Object result = memoized.testMethod(one);

		assertEquals(2, result);
		assertEquals(2, cache.get(Arrays.asList(one)));

	}
}