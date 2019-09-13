package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * This wraps a constructor, which is essentially a method.
 * @param <T>
 */
public class ConstructorMirror<T> extends AbstractMethodMirror {

	/**
	 * This is the method name for constructors in the JVM. It is the string "&lt;init&gt;".
	 */
	public static final String INIT = "<init>";

	public ConstructorMirror(ClassReferenceMirror parentClass, List<AnnotationMirror> annotations,
			ModifierMirror modifiers, ClassReferenceMirror type, String name, List<ClassReferenceMirror> params,
			boolean isVararg, boolean isSynthetic, String signature) {
		super(parentClass, annotations, modifiers, type, name, params, isVararg, isSynthetic, signature);
	}

	public ConstructorMirror(Constructor cons) {
		super(cons);
	}

	/* package */ ConstructorMirror(ClassReferenceMirror parentClass, ModifierMirror modifiers,
			ClassReferenceMirror type, String name, List<ClassReferenceMirror> params, boolean isVararg,
			boolean isSynthetic, String signature) {
		super(parentClass, modifiers, type, name, params, isVararg, isSynthetic, signature);
	}

	public ConstructorMirror(MethodMirror copy) {
		super(copy.getDeclaringClass(), copy.modifiers, copy.type, copy.name, copy.getParams(), copy.isVararg(),
				copy.isSynthetic(), copy.signature.toString());
		this.setLineNumber(copy.getLineNumber());
		if(!INIT.equals(copy.name)) {
			throw new IllegalArgumentException("Only constructors may be mirrored by "
					+ this.getClass().getSimpleName());
		}
	}

	/**
	 * Gets the Constructor object. This inherently loads the parent class using the default classloader.
	 *
	 * This also loads all parameter type's classes as well.
	 *
	 * @return
	 * @throws java.lang.ClassNotFoundException
	 */
	public Constructor<T> loadConstructor() throws ClassNotFoundException {
		return (Constructor<T>) loadConstructor(ConstructorMirror.class.getClassLoader(), true);
	}

	/**
	 * This loads the parent class, and returns the {@link Constructor} object. This also loads all parameter type's
	 * classes as well.
	 * <p>
	 * If this class was created with an actual Constructor, then that is simply returned.
	 *
	 * This also loads all parameter type's classes as well.
	 *
	 * @param loader
	 * @param initialize
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Constructor<T> loadConstructor(ClassLoader loader, boolean initialize) throws ClassNotFoundException {
		if(getExecutable() != null) {
			return (Constructor<T>) getExecutable();
		}
		Class parent = loadParentClass(loader, initialize);
		List<Class> cParams = new ArrayList<>();
		for(ClassReferenceMirror c : getParams()) {
			cParams.add(c.loadClass(loader, initialize));
		}
		try {
			return parent.getConstructor(cParams.toArray(new Class[cParams.size()]));
		} catch (NoSuchMethodException | SecurityException ex) {
			//There's really no way for any exception to happen here, so just rethrow
			throw new RuntimeException(ex);
		}
	}

}
