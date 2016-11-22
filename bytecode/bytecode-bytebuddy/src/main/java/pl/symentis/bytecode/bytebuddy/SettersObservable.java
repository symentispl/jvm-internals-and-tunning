package pl.symentis.bytecode.bytebuddy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public class SettersObservable {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
	
		Class<? extends JavaBean> dynamicType = new ByteBuddy()
		.subclass(JavaBean.class)
		.method(ElementMatchers.isSetter())
		.intercept(MethodDelegation.to(SettersObservable.class))
		.make()
		.load(SettersObservable.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
		.getLoaded();

		JavaBean javaBean = dynamicType.newInstance();
		
		javaBean.setName("Jarek");
		
	}
	
}
