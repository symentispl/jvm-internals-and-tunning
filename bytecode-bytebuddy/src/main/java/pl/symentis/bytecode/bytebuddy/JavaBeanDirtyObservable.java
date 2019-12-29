package pl.symentis.bytecode.bytebuddy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public class JavaBeanDirtyObservable {
	
	private final static ConcurrentMap<Class<?>, Class<?>> cache = new ConcurrentHashMap<>();

	public static <T> T observe(Class<T> clazz) throws Exception{
		
		Class<?> subclass = cache.computeIfAbsent(clazz, beanClass -> {
			return new ByteBuddy()
					.subclass(beanClass)
					.defineField("dirty", DirtyObject.class, Visibility.PUBLIC)
					.method(ElementMatchers.isSetter())
					.intercept(MethodDelegation.toField("dirty"))
					.make()
					.load(JavaBeanDirtyObservable.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
					.getLoaded();			
		});
		
		@SuppressWarnings("unchecked")
		T javaBean = (T) subclass.getConstructor().newInstance();
		javaBean.getClass().getField("dirty").set(javaBean, new DirtyObject());
		return javaBean;
	}
	
}
