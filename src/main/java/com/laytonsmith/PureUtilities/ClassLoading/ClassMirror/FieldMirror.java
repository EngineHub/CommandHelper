
package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * This class gathers information about a field, without actually loading 
 * the containing class into memory. Most of the methods in {@link java.lang.reflect.Field} are
 * available in this class (or have an equivalent Mirror version).
 */
public class FieldMirror extends AbstractElementMirror {
	private static final long serialVersionUID = 1L;
	private final Object value;
	
	public FieldMirror(List<AnnotationMirror> annotations, ModifierMirror modifiers, ClassReferenceMirror type, String name, Object value){
		super(annotations, modifiers, type, name);
		this.value = value;
	}
	
	public Object getValue(){
		return value;
	}

	@Override
	public String toString() {
		return StringUtils.Join(annotations, "\n") + (annotations.isEmpty()?"":"\n") + (modifiers.toString() 
				+ " " + type).trim() + " " + name + " = " + (value == null?"null":value.toString()) + ";";
	}
	
	// Package methods/constructor
	/* package */ FieldMirror(ModifierMirror modifiers, ClassReferenceMirror type, String name, Object value){
		super(null, modifiers, type, name);
		this.value = value;
		this.annotations = new ArrayList<AnnotationMirror>();
	}

}
