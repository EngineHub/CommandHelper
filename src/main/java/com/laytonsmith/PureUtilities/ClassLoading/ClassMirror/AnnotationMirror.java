
package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class AnnotationMirror implements Serializable {
	private static final long serialVersionUID = 1L;
	private ClassReferenceMirror type;
	private boolean visible;
	private List<AnnotationValue> values;
	
	public AnnotationMirror(ClassReferenceMirror type, boolean visible, List<AnnotationValue> values){
		this(type, visible);
		this.values = values;
	}
	
	/* package */ AnnotationMirror(ClassReferenceMirror type, boolean visible){
		this.type = type;
		this.visible = visible;
		this.values = new ArrayList<AnnotationValue>();
	}
	
	/* package */ void addAnnotationValue(String name, Object value){
		values.add(new AnnotationValue(name, value));
	}
	
	/**
	 * Returns the value for this annotation. Note that this won't resolve
	 * default annotations, as that requires actually loading the annotation class
	 * into memory. See {@link #getValueWithDefault} if you are ok with loading
	 * the annotation class into memory. Null is returned if this value doesn't exist.
	 * @param forName
	 * @return 
	 */
	public Object getValue(String forName){
		for(AnnotationValue value : values){
			if(value.name.equals(forName)){
				return value.value;
			}
		}
		return null;
	}
	
	/**
	 * Returns the list of defined values in this annotation. Note that this
	 * won't resolve default annotations, as that requires actually loading the
	 * annotation class into memory. See {@link #getDefinedValuesWithDefault} if you
	 * are ok with loading the annotation class into memory.
	 * @return 
	 */
	public List<String> getDefinedValues(){
		List<String> list = new ArrayList<String>();
		for(AnnotationValue value : values){
			list.add(value.name);
		}
		return list;
	}
	
	/**
	 * Gets the value of this annotation. If the value wasn't defined
	 * in this annotation, the default is returned by loading the annotation
	 * Class into memory, and finding the default, and returning that. Calling
	 * this method doesn't guarantee that the class will be loaded, however.
	 * If the value doesn't exist, at all, this will return null.
	 * @param value
	 * @return 
	 */
	public Object getValueWithDefault(String forName) throws ClassNotFoundException{
		Object value = getValue(forName);
		if(value != null){
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
	 * Loads the class into memory, and returns all the annotation value names.
	 * This includes default values that weren't specified by the actual instance
	 * of the annotation. The values returned from here must be used with
	 * {@link #getValueWithDefault} to ensure they will return a value properly.
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public List<String> getDefinedValuesWithDefault() throws ClassNotFoundException{
		List<String> ret = new ArrayList<String>();
		Class c = type.loadClass();
		for(Method m : c.getDeclaredMethods()){
			ret.add(m.getName());
		}
		return ret;
	}
	
	/**
	 * Returns the type of this annotation.
	 * @return 
	 */
	public ClassReferenceMirror getType(){
		return type;
	}
	
	/**
	 * Returns true if this annotation is visible.
	 * @return 
	 */
	public boolean isVisible(){
		return visible;
	}
	
	/**
	 * Gets a proxy annotation. When retrieving the annotation value,
	 * getValueWithDefault is called, though the class will for sure have
	 * already been loaded.
	 * @param <T>
	 * @param type
	 * @return 
	 * @throws IllegalArgumentException If AnnotationMirror doesn't represent the type
	 * requested.
	 */
	public <T extends Annotation> T getProxy(Class<T> type) throws IllegalArgumentException {
		if(!this.type.getJVMName().equals(ClassUtils.getJVMName(type))){
			throw new IllegalArgumentException();
		}
		return (T) Proxy.newProxyInstance(AnnotationMirror.class.getClassLoader(), new Class[]{type}, new InvocationHandler() {

			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return getValueWithDefault(method.getName());
			}
		});
	}

	@Override
	public String toString() {
		return "@" + type + "(" + StringUtils.Join(values, ", ") + ")";
	}
	
	public static class AnnotationValue implements Serializable {
		private static final long serialVersionUID = 1L;
		private String name;
		private Object value;
		
		public AnnotationValue(String name, Object value){
			this.name = name;
			this.value = value;
		}

		@Override
		public String toString() {
			if(value instanceof String){
				return name + " = " + StringUtils.toCodeString(value.toString());
			} else {
				return name + " = " + value.toString();
			}
		}
		
	}
}
