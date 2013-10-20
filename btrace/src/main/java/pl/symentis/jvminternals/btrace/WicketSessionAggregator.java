package pl.symentis.jvminternals.btrace;

import com.sun.btrace.aggregation.Aggregation;
import com.sun.btrace.aggregation.AggregationFunction;
import com.sun.btrace.annotations.*;

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class WicketSessionAggregator {

	public static Aggregation avg = newAggregation(AggregationFunction.AVERAGE);
	public static Aggregation count = newAggregation(AggregationFunction.COUNT);

	@OnMethod(clazz = "+org.apache.wicket.Session", method = "<init>", location = @Location(value = Kind.RETURN))
	public static void func(@Duration long duration) {
		addToAggregation(avg, duration);
		addToAggregation(count,1);
	}

	/**
	 * Methods annotated by @OnEvent can't have arguments
	 */
	@OnEvent
	public static void sharedMethod() {
		println("---summary---");
		printAggregation("avg sessions creeation time ", avg);
		printAggregation("number of created sessions ", count);
	}
}
