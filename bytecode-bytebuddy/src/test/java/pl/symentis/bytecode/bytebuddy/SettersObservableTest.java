package pl.symentis.bytecode.bytebuddy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SettersObservableTest {

	@Test
	void test() throws Exception {
		JavaBean bean = JavaBeanDirtyObservable.observe(JavaBean.class);
		bean.setName("Jarek");
		
		assertEquals( "Jarek", bean.getName());
		assertTrue(((DirtyObject)bean.getClass().getField("dirty").get(bean)).getDirty());
	}

}
