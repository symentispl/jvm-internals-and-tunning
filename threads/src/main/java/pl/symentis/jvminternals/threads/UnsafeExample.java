package pl.symentis.jvminternals.threads;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class UnsafeExample {

	private static final Unsafe UNSAFE;
	static {
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			UNSAFE = (Unsafe) theUnsafe.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int value;

	public UnsafeExample() {

	}

	public static void main(String[] args) throws NoSuchFieldException,
			SecurityException {

		int fieldOffset = UNSAFE.fieldOffset(UnsafeExample.class.getField("value"));

		System.out.println(fieldOffset);
		
		UnsafeExample m = new UnsafeExample();

		UNSAFE.compareAndSwapInt(m, fieldOffset, 1, 1);

		System.out.println(m.value);
	}
}
