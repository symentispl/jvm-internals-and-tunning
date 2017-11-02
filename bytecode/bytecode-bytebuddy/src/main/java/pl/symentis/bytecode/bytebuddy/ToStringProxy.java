package pl.symentis.bytecode.bytebuddy;

import static net.bytebuddy.matcher.ElementMatchers.named;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;

public class ToStringProxy {

	public static <T> Class<? extends T> toStringProxy(Class<T> clazz, String toString) {
		Class dynamicType = new ByteBuddy()
		.subclass(clazz)
		.method(named("toString"))
		.intercept(FixedValue.value(toString))
		.make()
		.load(ToStringProxy.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
		.getLoaded();
		return dynamicType;
	}

}
