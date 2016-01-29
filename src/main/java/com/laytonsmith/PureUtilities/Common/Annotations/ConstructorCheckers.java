package com.laytonsmith.PureUtilities.Common.Annotations;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.MethodMirror;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 
 */
public class ConstructorCheckers {

	public static void checkConstructors(){
		Set<Constructor> set = ClassDiscovery.getDefaultInstance().loadConstructorsWithAnnotation(MustIncludeConstructor.class);
		System.out.println(set);
	}
	
}
