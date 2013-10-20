package pl.symentis.jvminternals.btrace;

import com.sun.btrace.annotations.*;
import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class ThreadSpy {
	@OnMethod(clazz = "java.lang.Thread", method = "start", location = @Location(value = Kind.RETURN))
	public static void func(@Duration long duration) {
		println(concat("mamy nowy wątek, wystartowal", str(duration)));
	}

//	public static void sharedMethod(String msg) {
//		println(msg);
//	}
}
