package pl.symentis.bytecode.instrument;

import java.lang.instrument.Instrumentation;

import javassist.ClassPool;

public class Agent {

	public static void premain(String agentArgs, Instrumentation inst) {
		final ClassPool classPool = new ClassPool();
		inst.addTransformer(new MemoizeMethodTransformer(classPool,"pl.symentis.bytecode.memoize.Memoize"));
	}

}
