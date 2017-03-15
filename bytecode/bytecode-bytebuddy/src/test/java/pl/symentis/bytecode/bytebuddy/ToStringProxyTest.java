package pl.symentis.bytecode.bytebuddy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ToStringProxyTest {

	@Test
	public void should_proxy_to_string_call() throws Exception {
		
		MyClass myClass = new MyClass();
		
		System.out.println(myClass.toString());

		Class<? extends Object> proxyClass = ToStringProxy.toStringProxy(MyClass.class, "nazwa.pl");

		MyClass instance = (MyClass) proxyClass.newInstance();
		
		System.out.println(instance.toString());
		
		String string = instance.toString();

		assertEquals("nazwa.pl", string);

	}
}
