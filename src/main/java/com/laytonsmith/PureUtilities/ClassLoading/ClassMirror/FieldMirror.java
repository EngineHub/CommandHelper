package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.lang.reflect.Field;
import java.util.List;

/**
 * This class gathers information about a field, without actually loading the containing class into memory. Most of the
 * methods in {@link java.lang.reflect.Field} are available in this class (or have an equivalent Mirror version).
 */
public class FieldMirror extends AbstractElementMirror {

	private static final long serialVersionUID = 1L;
	private final Object value;
	private Field field;

	/**
	 * Creates a new FieldMirror based on an actual field, for easy comparisons.
	 *
	 * @param field
	 */
	public FieldMirror(Field field) {
		super(field);
		this.field = field;
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		Object value = null;
		try {
			// Try to get the value. This will work if the value is hardcoded, i.e. public int i = 5; but will fail
			// in some cases. In those cases, that's ok, we just will have a null value here.
			value = field.get(null);
		} catch (IllegalArgumentException | IllegalAccessException | NullPointerException ex) {
			//
		}
		this.value = value;
	}

	/**
	 * Creates a new FieldMirror based on the specified parameters.
	 *
	 * @param annotations
	 * @param modifiers
	 * @param type
	 * @param name
	 * @param value
	 * @param parent
	 * @param signature
	 */
	public FieldMirror(ClassReferenceMirror parent, List<AnnotationMirror> annotations, ModifierMirror modifiers,
			ClassReferenceMirror type, String name, Object value, String signature) {
		super(parent, annotations, modifiers, type, name, signature);
		this.value = value;
	}

	/**
	 * Gets the initial value of this field. If various conditions are not met, this returns null. Namely, if the field
	 * does not have an initial value, it is not an Integer, a Float, a Long, a Double or a String (for int, float, long
	 * or String fields respectively), or the field is not static.
	 *
	 * <p>
	 * For FieldMirrors created with an actual Field, the value is simply the current static field's value, and doesn't
	 * follow the rules listed above.
	 *
	 * @return
	 */
	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return StringUtils.Join(annotations, "\n") + (annotations.isEmpty() ? "" : "\n") + (modifiers.toString()
				+ " " + type).trim() + " " + name + " = " + (value == null ? "null" : value.toString()) + ";";
	}


	// Package methods/constructor
	/* package */ FieldMirror(ClassReferenceMirror parent, ModifierMirror modifiers, ClassReferenceMirror type,
			String name, Object value, String signature) {
		super(parent, null, modifiers, type, name, signature);
		this.value = value;
	}

	/**
	 * This loads the parent class, and returns the {@link Field} object. This also loads all field's type's class
	 * as well.
	 * <p>
	 * If this class was created with an actual Field, then that is simply returned.
	 *
	 * @return
	 * @throws java.lang.ClassNotFoundException
	 */
	public Field loadField() throws ClassNotFoundException {
		return this.loadField(FieldMirror.class.getClassLoader(), true);
	}

	/**
	 * This loads the parent class, and returns the {@link Method} object.
	 * <p>
	 * If this class was created with an actual Method, then that is simply returned.
	 *
	 * @param loader
	 * @param initialize
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Field loadField(ClassLoader loader, boolean initialize) throws ClassNotFoundException {
		if(field != null) {
			return field;
		}
		Class parent = loadParentClass(loader, initialize);
		NoSuchFieldException nsfe = null;
		do {
			try {
				field = parent.getDeclaredField(name);
				break;
			} catch (NoSuchFieldException ex) {
				nsfe = ex;
			} catch (SecurityException ex) {
				throw new RuntimeException(ex);
			}
			parent = parent.getSuperclass();
		} while(parent != null);
		// Try one last time
		try {
			field = loadParentClass(loader, initialize).getField(name);
		} catch (NoSuchFieldException ex) {
			nsfe = ex;
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		}

		if(field == null && nsfe != null) {
			throw new RuntimeException(nsfe);
		}
		return field;
	}

}
