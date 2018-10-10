package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * This class checks "instanceof" for native MethodScript objects, unlike the java "instanceof" keyword.
 */
public class InstanceofUtil {

	/**
	 * Returns true whether or not a given MethodScript value is an instance of the specified MethodScript type.
	 *
	 * @param value The value to check for
	 * @param instanceofThis The string type to check. This must be the fully qualified name.
	 * @return
	 */
	public static boolean isInstanceof(Mixed value, String instanceofThis) {
		Static.AssertNonNull(instanceofThis, "instanceofThis may not be null");
		if(instanceofThis.equals("auto")) {
			return true;
		}
		for(Class c : ClassUtils.getAllCastableClasses(value.getClass())) {
			String typeof = typeof(c);
			if(typeof != null && typeof.equals(instanceofThis)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns whether or not a given MethodScript value is an instanceof the specified MethodScript type.
	 * @param value
	 * @param instanceofThis
	 * @return 
	 */
	public static boolean isInstanceof(Mixed value, Class<? extends Mixed> instanceofThis) {
		String typeof = typeof(instanceofThis);
		return typeof == null ? false : isInstanceof(value, typeof);
	}

	/**
	 * Returns whether or not a given MethodScript value is an instance of the specified MethodScript type.
	 *
	 * @param value The value to check for
	 * @param instanceofThis The CClassType to check
	 * @return
	 */
	public static boolean isInstanceof(Mixed value, CClassType instanceofThis) {
		return isInstanceof(value, instanceofThis.val());
	}

	private static String typeof(Class<?> c) {
		typeof type = c.getAnnotation(typeof.class);
		if(type == null) {
			return null;
		} else {
			return type.value();
		}
	}

}
