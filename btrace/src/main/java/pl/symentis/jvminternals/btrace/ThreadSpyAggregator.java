package pl.symentis.jvminternals.btrace;

import com.sun.btrace.aggregation.Aggregation;
import com.sun.btrace.aggregation.AggregationFunction;
import com.sun.btrace.annotations.*;

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class ThreadSpyAggregator {

	public static Aggregation aggregation = newAggregation(AggregationFunction.AVERAGE);

	@OnMethod(clazz = "java.lang.Thread", method = "start", location = @Location(value = Kind.RETURN))
	public static void func(@Duration long duration) {
		addToAggregation(aggregation, duration);
	}

	@OnEvent
	public static void sharedMethod(String msg) {
		println("---summary---");
		printAggregation("avg threads start time ", aggregation);
	}
}
