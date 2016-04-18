package pl.symentis.bytecode.bytebuddy;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.io.File;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;

public class ToStringProxyFactory {

	public static <T> Class<? extends T> toStringProxy(Class<T> clazz, String toString) {
		Class dynamicType = new ByteBuddy()
		.redefine(File.class)
		.method(named("toString"))
		.intercept(FixedValue.value(toString))
		.make()
		.load(ToStringProxyFactory.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
		.getLoaded();
		return dynamicType;
	}

}
