
package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class gathers information about a method, without actually loading 
 * the containing class into memory. Most of the methods in {@link java.lang.reflect.Method} are
 * available in this class (or have an equivalent Mirror version).
 */
public class MethodMirror extends AbstractElementMirror {
	private static final long serialVersionUID = 1L;
	
	private final List<ClassReferenceMirror> params;
	private boolean isVararg = false;
	private boolean isSynthetic = false;
	
	private Method underlyingMethod = null;
	
	public MethodMirror(ClassReferenceMirror parentClass, List<AnnotationMirror> annotations, ModifierMirror modifiers, 
			ClassReferenceMirror type, String name, List<ClassReferenceMirror> params, boolean isVararg, boolean isSynthetic){
		super(parentClass, annotations, modifiers, type, name);
		this.params = params;
		this.isVararg = isVararg;
		this.isSynthetic = isSynthetic;
	}
	
	public MethodMirror(Method method){
		super(method);
		this.underlyingMethod = method;
		this.params = null;
	}
	
	/* package */ MethodMirror(ClassReferenceMirror parentClass, ModifierMirror modifiers, ClassReferenceMirror type, 
			String name, List<ClassReferenceMirror> params, boolean isVararg, boolean isSynthetic){
		super(parentClass, null, modifiers, type, name);
		annotations = new ArrayList<>();
		this.params = params;
		this.isVararg = isVararg;
		this.isSynthetic = isSynthetic;
	}
	
	/**
	 * Returns a list of params in this method.
	 * @return 
	 */
	public List<ClassReferenceMirror> getParams(){
		if(underlyingMethod != null){
			List<ClassReferenceMirror> list = new ArrayList<>();
			for(Class p : underlyingMethod.getParameterTypes()){
				list.add(ClassReferenceMirror.fromClass(p));
			}
			return list;
		}
		return new ArrayList<>(params);
	}
	
	/**
	 * Returns true if this method is vararg.
	 * @return 
	 */
	public boolean isVararg(){
		if(underlyingMethod != null){
			return underlyingMethod.isVarArgs();
		}
		return isVararg;
	}
	
	/**
	 * Returns true if this method is synthetic.
	 * @return 
	 */
	public boolean isSynthetic(){
		if(underlyingMethod != null){
			return underlyingMethod.isSynthetic();
		}
		return isSynthetic;
	}
	
	/**
	 * This loads the parent class, and returns the {@link Method} object.
	 * This also loads all parameter type's classes as well.
	 * <p>
	 * If this class was created with an actual Method, then that is simply returned.
	 * @return 
	 * @throws java.lang.ClassNotFoundException 
	 */
	public Method loadMethod() throws ClassNotFoundException{
		if(underlyingMethod != null){
			return underlyingMethod;
		}
		return loadMethod(MethodMirror.class.getClassLoader(), true);
	}
	
	/**
	 * This loads the parent class, and returns the {@link Method} object.
	 * This also loads all parameter type's classes as well.
	 * <p>
	 * If this class was created with an actual Method, then that is simply returned.
	 * @param loader
	 * @param initialize
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public Method loadMethod(ClassLoader loader, boolean initialize) throws ClassNotFoundException{
		if(underlyingMethod != null){
			return underlyingMethod;
		}
		ClassReferenceMirror p = getDeclaringClass();
		Objects.requireNonNull(p, "Declaring class is null!");
		Class parent = p.loadClass(loader, initialize);
		List<Class> cParams = new ArrayList<>();
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

	@Override
	public String toString() {
		if(underlyingMethod != null){
			return underlyingMethod.toString();
		}
		List<String> sParams = new ArrayList<>();
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
	
}
