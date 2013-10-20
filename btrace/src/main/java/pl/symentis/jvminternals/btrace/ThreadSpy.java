package pl.symentis.jvminternals.btrace;

import com.sun.btrace.annotations.*;
import static com.sun.btrace.BTraceUtils.*;

@BTrace
class ThreadSpy {
	@OnMethod(clazz = "java.lang.Thread", method = "start")
	void func() {
		sharedMethod("mamy nowy wątek");
	}

	void sharedMethod(String msg) {
		println(msg);
	}
}
