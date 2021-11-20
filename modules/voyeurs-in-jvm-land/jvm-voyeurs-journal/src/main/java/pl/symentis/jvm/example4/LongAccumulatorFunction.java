package pl.symentis.jvm.example4;

@FunctionalInterface
public interface LongAccumulatorFunction {

	long fold(long l, Record r);
	
}
