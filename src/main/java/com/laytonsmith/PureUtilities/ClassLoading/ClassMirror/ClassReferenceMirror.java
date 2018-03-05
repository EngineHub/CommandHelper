package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.ClassUtils;
import java.io.Serializable;

/**
 * A class reference mirror is a wrapper around a simple class name reference. It cannot directly get more information
 * about a class without actually loading it, so minimal information is available directly, though there is a method for
 * loading the actual class referenced, at which point more information could be retrieved.
 */
public class ClassReferenceMirror<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Returns a ClassReferenceMirror for a given real class. This is useful when doing comparisons.
	 *
	 * @param c
	 * @return
	 */
	public static ClassReferenceMirror fromClass(Class c) {
		return new ClassReferenceMirror(ClassUtils.getJVMName(c));
	}

	private final String name;

	/**
	 * The name should look similar to e.g.: "Ljava/lang/Object;" or "I"
	 *
	 * @param name The JVM binary name for this class.
	 */
	public ClassReferenceMirror(String name) {
		this.name = name;
	}

	/**
	 * Returns the java binary name for this class reference.
	 *
	 * @return
	 */
	public String getJVMName() {
		return name;
	}

	/**
	 * Loads the class into memory and returns the class object. For this call to succeed, the class must otherwise be
	 * on the class path. The standard class loader is used, and the class is initialized.
	 *
	 * @return
	 * @throws java.lang.ClassNotFoundException If the class can't be found
	 */
	public Class<T> loadClass() throws ClassNotFoundException {
		return loadClass(ClassReferenceMirror.class.getClassLoader(), true);
	}

	/**
	 * Loads the class into memory and returns the class object. For this call to succeed, the classloader specified
	 * must be able to find the class.
	 *
	 * @param loader
	 * @return
	 */
	public Class<T> loadClass(ClassLoader loader, boolean initialize) throws ClassNotFoundException {
		return ClassUtils.forCanonicalName(ClassUtils.getCommonNameFromJVMName(name), initialize, loader);
	}

	/**
	 * Returns true if this represents an array type.
	 *
	 * @return
	 */
	public boolean isArray() {
		return name.startsWith("[");
	}

	/**
	 * If this is an array type, returns the component type. For instance, for a String[], String would be returned.
	 * Null is returned if this isn't an array type.
	 *
	 * @return
	 */
	public ClassReferenceMirror getComponentType() {
		if(!isArray()) {
			return null;
		}
		return new ClassReferenceMirror(name.substring(1));
	}

	@Override
	public String toString() {
		return ClassUtils.getCommonNameFromJVMName(name);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 23 * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		final ClassReferenceMirror other = (ClassReferenceMirror) obj;
		return !((this.name == null) ? (other.name != null) : !this.name.equals(other.name));
	}

}
