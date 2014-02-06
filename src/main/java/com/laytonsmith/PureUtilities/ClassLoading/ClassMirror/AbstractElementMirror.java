
package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.ClassUtils;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
	
	private Field underlyingField = null;
	private Method underlyingMethod = null;
	
	protected AbstractElementMirror(Field field){
		this.underlyingField = field;
	}
	
	protected AbstractElementMirror(Method method){
		this.underlyingMethod = method;
	}
	
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
		if(underlyingField != null){
			return new ModifierMirror(underlyingField.getModifiers());
		}
		if(underlyingMethod != null){
			return new ModifierMirror(underlyingMethod.getModifiers());
		}
		return modifiers;
	}
	
	/**
	 * Gets the name of this field/method.
	 * @return 
	 */
	public String getName(){
		if(underlyingField != null){
			return underlyingField.getName();
		}
		if(underlyingMethod != null){
			return underlyingMethod.getName();
		}
		return name;
	}
	
	/**
	 * Gets the type of this field/method. For methods, this is
	 * the return type.
	 * @return 
	 */
	public ClassReferenceMirror getType(){
		if(underlyingField != null){
			return ClassReferenceMirror.fromClass(underlyingField.getType());
		}
		if(underlyingMethod != null){
			return ClassReferenceMirror.fromClass(underlyingMethod.getReturnType());
		}
		return type;
	}
	
	/**
	 * Returns a list of the annotations on this field/method.
	 * @return 
	 */
	public List<AnnotationMirror> getAnnotations(){
		if(underlyingField != null){
			List<AnnotationMirror> list = new ArrayList<>();
			for(Annotation a : underlyingField.getDeclaredAnnotations()){
				list.add(new AnnotationMirror(a));
			}
			return list;
		}
		if(underlyingMethod != null){
			List<AnnotationMirror> list = new ArrayList<>();
			for(Annotation a : underlyingMethod.getDeclaredAnnotations()){
				list.add(new AnnotationMirror(a));
			}
			return list;
		}
		return new ArrayList<>(annotations);
	}
	
	/**
	 * Gets the annotation on this field/method.
	 * @param annotation
	 * @return 
	 */
	public AnnotationMirror getAnnotation(Class<? extends Annotation> annotation){
		String jvmName = ClassUtils.getJVMName(annotation);
		for(AnnotationMirror a : getAnnotations()){
			if(a.getType().getJVMName().equals(jvmName)){
				return a;
			}
		}
		return null;
	}
	
	/**
	 * Returns true if this element has the specified annotation attached to it.
	 * @param annotation
	 * @return 
	 */
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
