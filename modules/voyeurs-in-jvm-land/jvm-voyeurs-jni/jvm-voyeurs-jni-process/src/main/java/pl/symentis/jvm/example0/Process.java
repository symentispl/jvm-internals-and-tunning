package pl.symentis.jvm.example0;

import java.io.IOException;

import cz.adamh.utils.NativeUtils;

public class Process {
	
	static {
		try {
			NativeUtils.loadLibraryFromJar("/resources/jvm-voyeurs-jni-native.so");
		} catch (IOException e) {
			throw new RuntimeException("unable to load native library",e);
		}
	}

	public native int getPid();

}
