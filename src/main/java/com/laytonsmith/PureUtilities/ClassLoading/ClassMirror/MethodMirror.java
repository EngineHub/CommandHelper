
package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This class gathers information about a method, without actually loading 
 * the containing class into memory. Most of the methods in {@link java.lang.reflect.Method} are
 * available in this class (or have an equivalent Mirror version).
 */
public class MethodMirror extends AbstractElementMirror {
	private static final long serialVersionUID = 1L;
	
	private final List<ClassReferenceMirror> params;
	private final ClassReferenceMirror parentClass;
	private boolean isVararg = false;
	private boolean isSynthetic = false;
	public MethodMirror(ClassReferenceMirror parentClass, List<AnnotationMirror> annotations, ModifierMirror modifiers, 
			ClassReferenceMirror type, String name, List<ClassReferenceMirror> params, boolean isVararg, boolean isSynthetic){
		super(annotations, modifiers, type, name);
		this.parentClass = parentClass;
		this.params = params;
		this.isVararg = isVararg;
		this.isSynthetic = isSynthetic;
	}
	
	/* package */ MethodMirror(ClassReferenceMirror parentClass, ModifierMirror modifiers, ClassReferenceMirror type, 
			String name, List<ClassReferenceMirror> params, boolean isVararg, boolean isSynthetic){
		super(null, modifiers, type, name);
		annotations = new ArrayList<AnnotationMirror>();
		this.parentClass = parentClass;
		this.params = params;
		this.isVararg = isVararg;
		this.isSynthetic = isSynthetic;
	}
	
	/**
	 * Returns a list of params in this method.
	 * @return 
	 */
	public List<ClassReferenceMirror> getParams(){
		return new ArrayList<ClassReferenceMirror>(params);
	}
	
	/**
	 * Returns true if this method is vararg.
	 * @return 
	 */
	public boolean isVararg(){
		return isVararg;
	}
	
	/**
	 * Returns true if this method is synthetic.
	 * @return 
	 */
	public boolean isSynthetic(){
		return isSynthetic;
	}
	
	/**
	 * This loads the parent class, and returns the {@link Method} object.
	 * This also loads all parameter type's classes as well.
	 * @return 
	 */
	public Method loadMethod() throws ClassNotFoundException{
		return loadMethod(MethodMirror.class.getClassLoader(), true);
	}
	
	/**
	 * This loads the parent class, and returns the {@link Method} object.
	 * This also loads all parameter type's classes as well.
	 * @param loader
	 * @param initialize
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public Method loadMethod(ClassLoader loader, boolean initialize) throws ClassNotFoundException{
		Class parent = parentClass.loadClass(loader, initialize);
		List<Class> cParams = new ArrayList<Class>();
		for(ClassReferenceMirror c : params){
			cParams.add(c.loadClass(loader, initialize));
		}
		try {
			return parent.getMethod(name, cParams.toArray(new Class[cParams.size()]));
		} catch (Exception ex) {
			//There's really no way for any exception to happen here, so just rethrow
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Returns a ClassReferenceMirror to the parent class.
	 * @return 
	 */
	public ClassReferenceMirror getDeclaringClass(){
		return parentClass;
	}

	@Override
	public String toString() {
		List<String> sParams = new ArrayList<String>();
		for(int i = 0; i < params.size(); i++){
			if(i == params.size() - 1 && isVararg){
				sParams.add(params.get(i).getComponentType().toString() + "...");
			} else {
				sParams.add(params.get(i).toString());
			}
		}
		return StringUtils.Join(annotations, "\n") + (annotations.isEmpty()?"":"\n") + (modifiers.toString() 
				+ " " + type).trim() + " " + name + "(" + StringUtils.Join(sParams, ", ") + "){}";
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (this.params != null ? this.params.hashCode() : 0);
		hash = 31 * hash + (this.parentClass != null ? this.parentClass.hashCode() : 0);
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
		final MethodMirror other = (MethodMirror) obj;
		if (this.params != other.params && (this.params == null || !this.params.equals(other.params))) {
			return false;
		}
		if (this.parentClass != other.parentClass && (this.parentClass == null || !this.parentClass.equals(other.parentClass))) {
			return false;
		}
		return true;
	}
	
	
	
}
