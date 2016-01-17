package pl.symentis.bytecode.bytebuddy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ToStringProxyFactoryTest {

	@Test
	public void should_proxy_to_string_call() throws Exception {

		Class<? extends Object> proxyClass = ToStringProxyFactory
				.toStringProxy(Object.class, "Hello world!!!");

		String string = proxyClass.newInstance().toString();

		assertEquals("Hello world!!!", string);

	}
}
