package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for managing the native class lists.
 */
public class NativeTypeList {

	/**
	 * Returns a list of all the known native classes.
	 * @return
	 */
	public static Set<String> getNativeTypeList(){
		Set<String> ret = new HashSet<>();
		for(ClassMirror<Mixed> c : ClassDiscovery.getDefaultInstance().getClassesWithAnnotationThatExtend(typeof.class, Mixed.class)){
			ret.add(c.loadAnnotation(typeof.class).value());
		}
		// Also add this one in
		ret.add("mixed");
		return ret;
	}

	/**
	 * Returns the java class for the given MethodScript object name. This cannot return anything of a type more
	 * specific than Mixed.
	 * @param methodscriptType
	 * @return
	 * @throws ClassNotFoundException If the class can't be found
	 */
	public static Class<Mixed> getNativeClass(String methodscriptType) throws ClassNotFoundException {
		for(ClassMirror<Mixed> c : ClassDiscovery.getDefaultInstance().getClassesWithAnnotationThatExtend(typeof.class, Mixed.class)){
			if(c.getAnnotation(typeof.class).getProxy(typeof.class).value().equals(methodscriptType)){
				return c.loadClass();
			}
		}
		throw new ClassNotFoundException("Could not find the class of type " + methodscriptType);
	}
}
