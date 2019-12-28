package pl.symentis.bytecode.instrument;

import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtField.Initializer;
import javassist.CtMethod;
import javassist.Modifier;

public class MemoizeMethodGenerator {

	private final ClassPool classPool;
	private String annotationClassname;

	public MemoizeMethodGenerator(ClassPool classPool, String annotationClassname) {
		this.classPool = classPool;
		this.annotationClassname = annotationClassname;
	}

	public CtClass generateMemoizedMethods(CtClass ctClass) throws Exception {
		CtMethod[] methods = getAnnotatedMethods(ctClass, annotationClassname);
		if (methods != null && methods.length > 0) {

			// generate static field with cached values
			CtClass weakHashMapClass = classPool.get("java.util.WeakHashMap");
			CtField f = new CtField(weakHashMapClass, "_memoizeCache", ctClass);

			// made this field public, just to simplify tests
			f.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
			ctClass.addField(f, Initializer.byNew(weakHashMapClass));
			
			// then copy each method and create delegate to it
			for (CtMethod method : methods) {

				CtMethod copiedMethod = copyMethod(ctClass, method);
				ctClass.addMethod(copiedMethod);

				// generate new body of annotated method
				
				
				method.setBody(
						"{" 
						+ "java.util.List key = java.util.Arrays.asList($args);"
						+ "java.lang.Object result = _memoizeCache.get(key);" 
						+ "if(result!=null){"
						+ " return ($r)result;" 
						+ "}" 
						+ "result = _"+method.getName()+"($$);" 
						+ "_memoizeCache.put(key,result);"
						+ "return ($r)result;}");
				ctClass.debugWriteFile();
			}

		}
		return ctClass;
	}

	private CtMethod[] getAnnotatedMethods(CtClass ctClass, String string) throws ClassNotFoundException {

		List<CtMethod> annotatedMethods = new ArrayList<>();
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();
		for (CtMethod method : ctMethods) {
			Object[] annotations = method.getAnnotations();
			for (Object annotation : annotations) {
				Class<?>[] interfaces = annotation.getClass().getInterfaces();
				for (Class<?> intrf : interfaces) {
					if (string.equals(intrf.getName())) {
						annotatedMethods.add(method);
					}
				}
			}
		}

		return annotatedMethods.toArray(new CtMethod[annotatedMethods.size()]);
	}

	private CtMethod copyMethod(CtClass ctClass, CtMethod ctMethod) throws CannotCompileException {
		CtMethod method = new CtMethod(ctMethod, ctClass, null);
		method.setName("_" + method.getName());
		method.setModifiers(Modifier.PRIVATE);
		return method;
	}

	public boolean hasMemoizedMethods(CtClass ctClass) throws ClassNotFoundException {
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();
		for (CtMethod method : ctMethods) {
			Object[] annotations = method.getAnnotations();
			for (Object annotation : annotations) {
				Class<?>[] interfaces = annotation.getClass().getInterfaces();
				for (Class<?> intrf : interfaces) {
					if (annotationClassname.equals(intrf.getName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

}