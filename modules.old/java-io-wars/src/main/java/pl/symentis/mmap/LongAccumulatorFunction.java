package pl.symentis.mmap;

@FunctionalInterface
public interface LongAccumulatorFunction {

	long fold(long l, Record r);
	
}
