package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This class gathers information about a method, without actually loading the containing class into memory. Most of the
 * methods in {@link java.lang.reflect.Method} are available in this class (or have an equivalent Mirror version).
 */
public class MethodMirror extends AbstractMethodMirror {

	private static final long serialVersionUID = 2L;

	public MethodMirror(ClassReferenceMirror parentClass, List<AnnotationMirror> annotations, ModifierMirror modifiers,
			ClassReferenceMirror type, String name, List<ClassReferenceMirror> params, boolean isVararg, boolean isSynthetic) {
		super(parentClass, annotations, modifiers, type, name, params, isVararg, isSynthetic);
	}

	public MethodMirror(Method method) {
		super(method);
	}

	/* package */ MethodMirror(ClassReferenceMirror parentClass, ModifierMirror modifiers, ClassReferenceMirror type,
			String name, List<ClassReferenceMirror> params, boolean isVararg, boolean isSynthetic) {
		super(parentClass, null, modifiers, type, name, params, isVararg, isSynthetic);
	}

	/**
	 * This loads the parent class, and returns the {@link Method} object. This also loads all parameter type's classes
	 * as well.
	 * <p>
	 * If this class was created with an actual Method, then that is simply returned.
	 *
	 * @return
	 * @throws java.lang.ClassNotFoundException
	 */
	public Method loadMethod() throws ClassNotFoundException {
		if(getExecutable() != null) {
			return (Method) getExecutable();
		}
		return loadMethod(MethodMirror.class.getClassLoader(), true);
	}

	/**
	 * This loads the parent class, and returns the {@link Method} object. This also loads all parameter type's classes
	 * as well.
	 * <p>
	 * If this class was created with an actual Method, then that is simply returned.
	 *
	 * @param loader
	 * @param initialize
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Method loadMethod(ClassLoader loader, boolean initialize) throws ClassNotFoundException {
		if(getExecutable() != null) {
			return (Method) getExecutable();
		}

		Class parent = loadParentClass(loader, initialize);
		List<Class> cParams = new ArrayList<>();
		for(ClassReferenceMirror c : getParams()) {
			cParams.add(c.loadClass(loader, initialize));
		}
		try {
			return parent.getMethod(name, cParams.toArray(new Class[cParams.size()]));
		} catch (NoSuchMethodException | SecurityException ex) {
			//There's really no way for any exception to happen here, so just rethrow
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof MethodMirror)) {
			return false;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
