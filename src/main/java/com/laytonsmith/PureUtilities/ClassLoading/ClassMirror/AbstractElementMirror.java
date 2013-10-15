
package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.ClassUtils;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class AbstractElementMirror implements Serializable {
	private static final long serialVersionUID = 1L;
	protected ModifierMirror modifiers;
	protected String name;
	protected ClassReferenceMirror type;
	protected List<AnnotationMirror> annotations;
	
	protected AbstractElementMirror(List<AnnotationMirror> annotations, ModifierMirror modifiers, ClassReferenceMirror type, String name){
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.type = type;
		this.name = name;
	}

	/**
	 * Gets the modifiers on this field/method.
	 * @return 
	 */
	public ModifierMirror getModifiers(){
		return modifiers;
	}
	
	/**
	 * Gets the name of this field/method.
	 * @return 
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Gets the type of this field/method. For methods, this is
	 * the return type.
	 * @return 
	 */
	public ClassReferenceMirror getType(){
		return type;
	}
	
	/**
	 * Returns a list of the annotations on this field/method.
	 * @return 
	 */
	public List<AnnotationMirror> getAnnotations(){
		return new ArrayList<AnnotationMirror>(annotations);
	}
	
	/**
	 * Gets the annotation on this field/method.
	 * @param annotation
	 * @return 
	 */
	public AnnotationMirror getAnnotation(Class<? extends Annotation> annotation){
		String jvmName = ClassUtils.getJVMName(annotation);
		for(AnnotationMirror a : annotations){
			if(a.getType().getJVMName().equals(jvmName)){
				return a;
			}
		}
		return null;
	}
	
	public boolean hasAnnotation(Class<? extends Annotation> annotation){
		return getAnnotation(annotation) != null;
	}
	
	/**
	 * Loads the corresponding Annotation type for this field
	 * or method. This actually loads the Annotation class into memory.
	 * This is equivalent to getAnnotation(type).getProxy(type), however
	 * this checks for null first, and returns null instead of causing a NPE.
	 * @param <T>
	 * @param type
	 * @return 
	 */
	public <T extends Annotation> T loadAnnotation(Class<T> type) {
		AnnotationMirror mirror = getAnnotation(type);
		if(mirror == null){
			return null;
		}
		return mirror.getProxy(type);
	}
	
	/* package */ void addAnnotation(AnnotationMirror annotation){
		annotations.add(annotation);
	}
}
