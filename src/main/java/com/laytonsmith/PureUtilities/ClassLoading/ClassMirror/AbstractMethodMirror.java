package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An {@link AbstractMethodMirror} encompasses both methods and constructors.
 */
public abstract class AbstractMethodMirror extends AbstractElementMirror {

	private static final long serialVersionUID = 1L;

	private final List<ClassReferenceMirror> params;
	private boolean isVararg = false;
	private boolean isSynthetic = false;

	/**
	 * TODO: Once we switch to Java 1.8, this should be replaced by java.lang.reflect.Executable.
	 */
	private Member underlyingMethod = null;

	public AbstractMethodMirror(ClassReferenceMirror parentClass, List<AnnotationMirror> annotations, ModifierMirror modifiers,
			ClassReferenceMirror type, String name, List<ClassReferenceMirror> params, boolean isVararg, boolean isSynthetic) {
		super(parentClass, annotations, modifiers, type, name);
		Objects.requireNonNull(params, "params cannot be null");
		this.params = params;
		this.isVararg = isVararg;
		this.isSynthetic = isSynthetic;
	}

	public AbstractMethodMirror(Member method) {
		super(method);
		this.underlyingMethod = method;
		this.params = null;
	}

	/* package */ AbstractMethodMirror(ClassReferenceMirror parentClass, ModifierMirror modifiers, ClassReferenceMirror type,
			String name, List<ClassReferenceMirror> params, boolean isVararg, boolean isSynthetic) {
		super(parentClass, null, modifiers, type, name);
		annotations = new ArrayList<>();
		Objects.requireNonNull(params, "params cannot be null");
		this.params = params;
		this.isVararg = isVararg;
		this.isSynthetic = isSynthetic;
	}

	/**
	 * Returns a list of params in this method.
	 *
	 * @return
	 */
	public List<ClassReferenceMirror> getParams() {
		if(underlyingMethod != null) {
			List<ClassReferenceMirror> list = new ArrayList<>();
			for(Class p : ((Method) underlyingMethod).getParameterTypes()) {
				list.add(ClassReferenceMirror.fromClass(p));
			}
			return list;
		}
		return new ArrayList<>(params);
	}

	/**
	 * Returns true if this method is vararg.
	 *
	 * @return
	 */
	public boolean isVararg() {
		if(underlyingMethod != null) {
			return ((Method) underlyingMethod).isVarArgs();
		}
		return isVararg;
	}

	/**
	 * Returns true if this method is synthetic.
	 *
	 * @return
	 */
	public boolean isSynthetic() {
		if(underlyingMethod != null) {
			return underlyingMethod.isSynthetic();
		}
		return isSynthetic;
	}

	@Override
	public String toString() {
		if(underlyingMethod != null) {
			return underlyingMethod.toString();
		}
		List<String> sParams = new ArrayList<>();
		for(int i = 0; i < params.size(); i++) {
			if(i == params.size() - 1 && isVararg) {
				sParams.add(params.get(i).getComponentType().toString() + "...");
			} else {
				sParams.add(params.get(i).toString());
			}
		}
		return StringUtils.Join(annotations, "\n") + (annotations.isEmpty() ? "" : "\n") + (modifiers.toString()
				+ " " + type).trim() + " " + name + "(" + StringUtils.Join(sParams, ", ") + "){}";
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof MethodMirror)) {
			return false;
		}
		if(!super.equals(obj)) {
			return false;
		}
		AbstractMethodMirror m = (AbstractMethodMirror) obj;
		return Objects.equals(this.params, m.params)
				&& this.isVararg == m.isVararg
				&& this.isSynthetic == m.isSynthetic;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 31 * hash + super.hashCode();
		hash = 31 * hash + Objects.hashCode(this.params);
		hash = 31 * hash + (this.isVararg ? 1 : 0);
		hash = 31 * hash + (this.isSynthetic ? 1 : 0);
		return hash;
	}

	/**
	 * Returns the underlying executable (or null, if it was constructed artificially).
	 *
	 * @return
	 */
	protected Member getExecutable() {
		return (Member) underlyingMethod;
	}

	/**
	 * Loads the class that contains this method, using the default class loader.
	 *
	 * @return
	 * @throws java.lang.ClassNotFoundException
	 */
	public Class loadParentClass() throws ClassNotFoundException {
		return loadParentClass(AbstractMethodMirror.class.getClassLoader(), true);
	}

	/**
	 * Loads the class that contains this method, using the default class loader.
	 *
	 * @param loader
	 * @param initialize
	 * @return
	 * @throws java.lang.ClassNotFoundException
	 */
	public Class loadParentClass(ClassLoader loader, boolean initialize) throws ClassNotFoundException {
		ClassReferenceMirror p = getDeclaringClass();
		Objects.requireNonNull(p, "Declaring class is null!");
		return p.loadClass(loader, initialize);
	}
}
