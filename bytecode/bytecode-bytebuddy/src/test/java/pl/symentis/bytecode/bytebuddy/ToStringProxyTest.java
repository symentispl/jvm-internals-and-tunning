package pl.symentis.bytecode.bytebuddy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ToStringProxyTest {

	@Test
	public void should_proxy_to_string_call() throws Exception {

		Class<? extends Object> proxyClass = ToStringProxy
				.toStringProxy(Object.class, "Hello world!!!");

		String string = proxyClass.newInstance().toString();

		assertEquals("Hello world!!!", string);

	}
}
