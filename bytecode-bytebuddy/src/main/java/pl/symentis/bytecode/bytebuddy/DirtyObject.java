package pl.symentis.bytecode.bytebuddy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

public class DirtyObject {

	private boolean dirty;

	public boolean getDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public void markDirty(
			@SuperCall Callable<?> supercall, 
			@Origin Method method, 
			@Argument(0) Object args)
			throws Exception {
		System.out.println(String.format("method call %s(%s)", method, args));
		dirty=true;
		supercall.call();
	}

}
