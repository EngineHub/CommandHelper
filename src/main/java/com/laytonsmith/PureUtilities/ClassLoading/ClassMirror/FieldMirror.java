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

	/**
	 * Creates a new FieldMirror based on an actual field, for easy comparisons.
	 *
	 * @param field
	 */
	public FieldMirror(Field field) {
		super(field);
		Object value = null;
		try {
			value = field.get(null);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
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
	 */
	public FieldMirror(ClassReferenceMirror parent, List<AnnotationMirror> annotations, ModifierMirror modifiers, ClassReferenceMirror type, String name, Object value) {
		super(parent, annotations, modifiers, type, name);
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
	/* package */ FieldMirror(ClassReferenceMirror parent, ModifierMirror modifiers, ClassReferenceMirror type, String name, Object value) {
		super(parent, null, modifiers, type, name);
		this.value = value;
	}

}
