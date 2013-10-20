package pl.symentis.jvminternals.btrace;

import com.sun.btrace.annotations.*;
import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class ThreadSpy {
	@OnMethod(clazz = "java.lang.Thread", method = "start", location = @Location(value = Kind.RETURN))
	public static void func(@Self Thread thread, @Duration long duration) {
		println(concat("thread started id ", str(threadId(thread))));
		println(concat("thread started in ", str(duration)));
	}

}
