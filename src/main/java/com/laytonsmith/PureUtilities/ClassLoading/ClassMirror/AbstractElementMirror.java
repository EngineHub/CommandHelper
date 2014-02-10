
package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.ClassUtils;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is the superclass of any element type, such as a field or method.
 */
class AbstractElementMirror implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * Any modifiers on the element
	 */
	protected final ModifierMirror modifiers;
	/**
	 * The name of the element
	 */
	protected final String name;
	/**
	 * The type of the element, or in the case of methods or other
	 * composite types, the return type.
	 */
	protected final ClassReferenceMirror type;
	/**
	 * Any annotations on the element. This isn't final, because
	 * the fields and methods are created before they necessarily know their annotations.
	 */
	protected List<AnnotationMirror> annotations;
	/**
	 * The parent class of the element
	 */
	protected final ClassReferenceMirror parent;
	
	protected AbstractElementMirror(Field field){
		Objects.requireNonNull(field);
		this.type = ClassReferenceMirror.fromClass(field.getType());
		this.modifiers = new ModifierMirror(field.getModifiers());
		this.name = field.getName();
		List<AnnotationMirror> list = new ArrayList<>();
		for(Annotation a : field.getDeclaredAnnotations()){
			list.add(new AnnotationMirror(a));
		}
		this.annotations = list;
		this.parent = ClassReferenceMirror.fromClass(field.getDeclaringClass());
	}
	
	protected AbstractElementMirror(Method method){
		Objects.requireNonNull(method);
		this.type = ClassReferenceMirror.fromClass(method.getReturnType());
		this.modifiers = new ModifierMirror(method.getModifiers());
		this.name = method.getName();
		List<AnnotationMirror> list = new ArrayList<>();
		for(Annotation a : method.getDeclaredAnnotations()){
			list.add(new AnnotationMirror(a));
		}
		this.annotations = list;
		this.parent = ClassReferenceMirror.fromClass(method.getDeclaringClass());
	}
	
	protected AbstractElementMirror(ClassReferenceMirror parent, List<AnnotationMirror> annotations, ModifierMirror modifiers, ClassReferenceMirror type, String name){
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.type = type;
		this.name = name;
		this.parent = parent;
		Objects.requireNonNull(parent);
		Objects.requireNonNull(annotations);
		Objects.requireNonNull(modifiers);
		Objects.requireNonNull(type);
		Objects.requireNonNull(name);
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
	
	/**
	 * Returns the class that this is declared in.
	 * @return 
	 */
	public ClassReferenceMirror getDeclaringClass(){
		return parent;
	}
	
	/* package */ void addAnnotation(AnnotationMirror annotation){
		annotations.add(annotation);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 89 * hash + Objects.hashCode(this.name);
		hash = 89 * hash + Objects.hashCode(this.parent);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AbstractElementMirror other = (AbstractElementMirror) obj;
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		if (!Objects.equals(this.parent, other.parent)) {
			return false;
		}
		return true;
	}
	
	
}
