package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassReferenceMirror;
import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * This class checks "instanceof" for native MethodScript objects, unlike the java "instanceof" keyword.
 */
public class InstanceofUtil {

	public static boolean isInstanceof(Mixed value, String instanceofThis){
		for(Class c : ClassUtils.getAllCastableClasses(value.getClass())){
			String typeof = typeof(c);
			if(typeof != null && typeof.equals(instanceofThis)){
				return true;
			}
		}
		return false;
	}

	private static String typeof(Class<?> c){
		typeof type = c.getAnnotation(typeof.class);
		if(type == null){
			return null;
		} else {
			return type.value();
		}
	}

}
