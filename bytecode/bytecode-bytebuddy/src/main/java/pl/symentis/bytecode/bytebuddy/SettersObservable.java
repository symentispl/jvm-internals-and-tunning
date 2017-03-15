package pl.symentis.bytecode.bytebuddy;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Callable;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

public class SettersObservable {

	public static void premain(String[] args, Instrumentation inst) throws InstantiationException, IllegalAccessException {
	
		
		AgentBuilder.
			Default.
			of().
			installOn(inst);
		
		Class<? extends JavaBean> dynamicType = new ByteBuddy()
		.subclass(JavaBean.class)
		.method(ElementMatchers.isSetter())
		.intercept(MethodDelegation.to(SettersObservable.class))
		.make()
		.load(SettersObservable.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
		.getLoaded();

		JavaBean javaBean = dynamicType.newInstance();
		
		javaBean.setName("Jarek");
		
		System.out.println(javaBean.getName());
		
	}
	
	public static void _setterListener(@SuperCall Callable supercall, @Argument(0) Object args) throws Exception{
		System.out.println("wołam setter z paremetrem "+ args);
		supercall.call();
	}
	
}
