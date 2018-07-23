package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an Annotation. Most features available to annotations are available here, though finding the default value
 * of an annotation does require loading the annotation.
 */
public class AnnotationMirror implements Serializable {

	private static final long serialVersionUID = 1L;
	private final ClassReferenceMirror type;
	private final boolean visible;
	private final List<AnnotationValue> values;

	/**
	 * Creates a new AnnotationMirror based an a loaded {@link Annotation}.
	 *
	 * @param annotation
	 */
	public AnnotationMirror(Annotation annotation) {
		this.type = ClassReferenceMirror.fromClass(annotation.annotationType());
		this.visible = true;
		values = new ArrayList<>();
		for(Method m : annotation.annotationType().getDeclaredMethods()) {
			try {
				values.add(new AnnotationValue(m.getName(), m.invoke(annotation)));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/* package */ AnnotationMirror(ClassReferenceMirror type, boolean visible) {
		this.type = type;
		this.visible = visible;
		this.values = new ArrayList<>();
	}

	/* package */ void addAnnotationValue(String name, Object value) {
		values.add(new AnnotationValue(name, value));
	}

	/**
	 * Returns the value for this annotation. Note that this won't resolve default annotations, as that requires
	 * actually loading the annotation class into memory. See {@link #getValueWithDefault} if you are ok with loading
	 * the annotation class into memory. Null is returned if this value doesn't exist.
	 *
	 * If the underlying value's type is of type Class, the class name of it is returned as a String, which you can
	 * then choose to load yourself (with either {@link ClassDiscovery#forName} or {@link Class#forName}). This is done
	 * to prevent loading classes referenced in annotations by default.
	 *
	 * @param forName
	 * @return
	 */
	public Object getValue(String forName) {
		for(AnnotationValue value : values) {
			if(value.name.equals(forName)) {
				return value.value;
			}
		}
		return null;
	}

	/**
	 * Returns the list of defined values in this annotation. Note that this won't resolve default annotations, as that
	 * requires actually loading the annotation class into memory. See {@link #getDefinedValuesWithDefault} if you are
	 * ok with loading the annotation class into memory.
	 *
	 * @return
	 */
	public List<String> getDefinedValues() {
		List<String> list = new ArrayList<>();
		for(AnnotationValue value : values) {
			list.add(value.name);
		}
		return list;
	}

	/**
	 * Gets the value of this annotation. If the value wasn't defined in this annotation, the default is returned by
	 * loading the annotation Class into memory, and finding the default, and returning that. Calling this method
	 * doesn't guarantee that the class will be loaded, however. If the value doesn't exist, at all, this will return
	 * null.
	 *
	 * @param forName
	 * @return
	 * @throws java.lang.ClassNotFoundException
	 */
	public Object getValueWithDefault(String forName) throws ClassNotFoundException {
		Object value = getValue(forName);
		if(value != null) {
			return value;
		}
		//Nope, have to load it.
		Class c = type.loadClass();
		try {
			Method m = c.getMethod(forName);
			return m.getDefaultValue();
		} catch (NoSuchMethodException ex) {
			return null;
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Loads the class into memory, and returns all the annotation value names. This includes default values that
	 * weren't specified by the actual instance of the annotation. The values returned from here must be used with
	 * {@link #getValueWithDefault} to ensure they will return a value properly.
	 *
	 * @return
	 * @throws ClassNotFoundException
	 */
	public List<String> getDefinedValuesWithDefault() throws ClassNotFoundException {
		List<String> ret = new ArrayList<>();
		Class c = type.loadClass();
		for(Method m : c.getDeclaredMethods()) {
			ret.add(m.getName());
		}
		return ret;
	}

	/**
	 * Returns the type of this annotation.
	 *
	 * @return
	 */
	public ClassReferenceMirror getType() {
		return type;
	}

	/**
	 * Returns true if this annotation is visible.
	 *
	 * @return
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Gets a proxy annotation. When retrieving the annotation value, getValueWithDefault is called, and the
	 * annotation's Class will for sure have already been loaded.
	 *
	 * This allows for annotation values to be read from an element without having to actually load that element (just
	 * the annotation Class is loaded), and allowing the type safe checks of compile time.
	 *
	 * @param <T>
	 * @param type
	 * @return
	 * @throws IllegalArgumentException If AnnotationMirror doesn't represent the type requested.
	 */
	public <T extends Annotation> T getProxy(Class<T> type) throws IllegalArgumentException {
		if(!this.type.getJVMName().equals(ClassUtils.getJVMName(type))) {
			throw new IllegalArgumentException();
		}

		return (T) Proxy.newProxyInstance(AnnotationMirror.class.getClassLoader(), new Class[]{type}, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if(("equals".equals(method.getName()) && matches(args, Object.class))
						|| ("hashCode".equals(method.getName()) && matches(args))
						|| ("toString".equals(method.getName()) && matches(args))
						|| ("wait".equals(method.getName()) && matches(args))
						|| ("wait".equals(method.getName()) && matches(args, long.class))
						|| ("wait".equals(method.getName()) && matches(args, long.class, int.class))
						|| ("getClass".equals(method.getName()) && matches(args))
						|| ("notify".equals(method.getName()) && matches(args))
						|| ("notifyAll".equals(method.getName()) && matches(args))
						|| ("finalize".equals(method.getName()) && matches(args))
						|| ("clone".equals(method.getName()) && matches(args))) {
					// Currently, we just throw an exception, because they are
					// actual methods defined in Object, not annotation values.
					// I don't know how to make this work correctly yet.
					throw new RuntimeException("The " + method.getName() + " method cannot be called on Annotation Proxies yet.");
				}
				return getValueWithDefault(method.getName());
			}
		});
	}

	private static boolean matches(Object[] args, Class... types) {
		if(args == null) {
			return types.length == 0;
		}

		if(args.length != types.length) {
			return false;
		}
		for(int i = 0; i < args.length; i++) {
			//Can't just use == here, since the class might be a subclass
			if(!types[i].isAssignableFrom(args[i].getClass())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "@" + type + "(" + StringUtils.Join(values, ", ") + ")";
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(this.type);
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
		final AnnotationMirror other = (AnnotationMirror) obj;
		if(!Objects.equals(this.type, other.type)) {
			return false;
		}
		return true;
	}

	private static class AnnotationValue implements Serializable {

		private static final long serialVersionUID = 1L;
		private String name;
		private Object value;

		public AnnotationValue(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String toString() {
			if(value instanceof String) {
				return name + " = " + StringUtils.toCodeString(value.toString());
			} else {
				return name + " = " + value.toString();
			}
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 29 * hash + Objects.hashCode(this.name);
			hash = 29 * hash + Objects.hashCode(this.value);
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
			final AnnotationValue other = (AnnotationValue) obj;
			if(!Objects.equals(this.name, other.name)) {
				return false;
			}
			if(!Objects.equals(this.value, other.value)) {
				return false;
			}
			return true;
		}

	}
}
