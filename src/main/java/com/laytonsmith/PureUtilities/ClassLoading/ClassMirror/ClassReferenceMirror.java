package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ClassUtils;
import java.io.Serializable;
import java.util.Arrays;

/**
 * A class reference mirror is a wrapper around a simple class name reference. It cannot directly get more information
 * about a class without actually loading it, so minimal information is available directly, though there is a method for
 * loading the actual class referenced, at which point more information could be retrieved.
 * @param <T>
 */
public class ClassReferenceMirror<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * A ClassReferenceMirror that represents the wildcard class. This can only naturally occur in generic declarations,
	 * and is not a real class that can be loaded. Calls against this object to loadClass will fail with
	 * {@link ClassNotFoundException}.
	 */
	public static final ClassReferenceMirror WILDCARD = new ClassReferenceMirror("*");

	/**
	 * Returns a ClassReferenceMirror for a given real class. This is useful when doing comparisons. Note that
	 * generic data is lost during compilation, and therefore this method is not suitable when doing comparisons
	 * that include generic data. The generic data is not included in the equals or hashCode comparison, however.
	 *
	 * @param c
	 * @return
	 */
	public static ClassReferenceMirror fromClass(Class c) {
		return new ClassReferenceMirror(ClassUtils.getJVMName(c));
	}

	private final String name;
	private final String templateTypeName;
	private final ClassReferenceMirror[] genericParameters;

	/**
	 * Constructs a new ClassReferenceMirror, using the java class binary name.
	 *
	 * @param name The JVM binary name for this class. The name should look similar to e.g.: "Ljava/lang/Object;"
	 * or "I".
	 */
	public ClassReferenceMirror(String name) {
		this.name = name;
		this.genericParameters = new ClassReferenceMirror[0];
		templateTypeName = null;
	}

	/**
	 * Constructs a ClassReferenceMirror which has generic parameters.
	 * @param name
	 * @param genericParameters
	 */
	public ClassReferenceMirror(String name, ClassReferenceMirror[] genericParameters) {
		this.name = name;
		this.genericParameters = genericParameters;
		templateTypeName = null;
	}

	/**
	 * Constructs a ClassReferenceMirror which is a template type definition. This is only present on methods, and
	 * only if they define a template type, for instance in the method definition {@code <T> T method(Class<T>)}
	 * then the templateTypeName would be {@code T} and the name would be {@code Ljava/lang/Object;}. In the example
	 * {@code <J extends List> J method(Class<J>)} the templateTypeName would be {@code J} and the name would
	 * be {@code Ljava/util/List;}
	 * @param name
	 * @param templateTypeName
	 */
	public ClassReferenceMirror(String name, String templateTypeName) {
		this.name = name;
		this.genericParameters = null;
		this.templateTypeName = templateTypeName;
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
		return loadClass(ClassDiscovery.getDefaultInstance().getDefaultClassLoader(), true);
	}

	/**
	 * Loads the class into memory and returns the class object. For this call to succeed, the classloader specified
	 * must be able to find the class.
	 *
	 * @param loader
	 * @param initialize
	 * @return
	 * @throws java.lang.ClassNotFoundException
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
	 * Returns true if this is a template type declaration. Template definitions cannot have generic parameters, so
	 * getGenerics will return null if this method returns true.
	 * <p>
	 * A template usage is a usage of the a template declaration, which is defined elsewhere. A template definition is
	 * where that template type is defined, which may be either on a method i.e. {@code <T> T method()} or on the class
	 * definition i.e. {@code class MyClass<T>{}}.
	 * @return
	 */
	public boolean isTemplateDefinition() {
		return this.templateTypeName != null;
	}

	/**
	 * Returns the name of the template type declaration. This will be null if this is not a template type declaration.
	 * This is different than this object being a template usage.
	 * <p>
	 * A template usage is a usage of the a template declaration, which is defined elsewhere. A template definition is
	 * where that template type is defined, which may be either on a method i.e. {@code <T> T method()} or on the class
	 * definition i.e. {@code class MyClass<T>{}}.
	 * @return
	 */
	public String getTemplateTypeName() {
		return this.templateTypeName;
	}

	/**
	 * If this is a template type used with generics. This is different than this object being a template definition.
	 * <p>
	 * A template usage is a usage of the a template declaration, which is defined elsewhere. A template definition is
	 * where that template type is defined, which may be either on a method i.e. {@code <T> T method()} or on the class
	 * definition i.e. {@code class MyClass<T>{}}.
	 * @return
	 */
	public boolean isTemplateUsage() {
		return name.startsWith("T");
	}

	/**
	 * Returns the generic parameters that this reference was declared with.
	 * @return
	 */
	public ClassReferenceMirror[] getGenerics() {
		return Arrays.copyOf(genericParameters, genericParameters.length);
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
		String ret;
		if(isTemplateDefinition()) {

		}
		ret = ClassUtils.getCommonNameFromJVMName(name);
		if(genericParameters != null && genericParameters.length != 0) {
			ret += "<";
			boolean first = true;
			for(ClassReferenceMirror r : genericParameters) {
				if(!first) {
					ret += ", ";
				}
				first = false;
				ret += r.toString();
			}
			ret += ">";
		}
		return ret;
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
