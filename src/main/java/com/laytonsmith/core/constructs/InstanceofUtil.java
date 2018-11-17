package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
	public static boolean isInstanceof(Mixed value, FullyQualifiedClassName instanceofThis) {
		Static.AssertNonNull(instanceofThis, "instanceofThis may not be null");
		if(instanceofThis.getFQCN().equals("auto")) {
			return true;
		}
		for(CClassType c : getAllCastableClasses(value.typeof())) {
			FullyQualifiedClassName typeof = c.getFQCN();
			if(typeof != null && typeof.equals(instanceofThis)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a list of all classes that the specified class can be validly cast to. This includes all super classes,
	 * as well as all interfaces (and superclasses of those interfaces, etc) and java.lang.Object, as well as the class
	 * itself.
	 *
	 * @param c The class to search for.
	 * @return
	 */
	public static Set<CClassType> getAllCastableClasses(CClassType c) {
		Set<CClassType> ret = new HashSet<>();
		getAllCastableClassesWithBlacklist(c, ret);
		return ret;
	}

	/**
	 * Private version of {@link #getAllCastableClasses(java.lang.Class)}
	 *
	 * @param c
	 * @param blacklist
	 * @return
	 */
	private static Set<CClassType> getAllCastableClassesWithBlacklist(CClassType c, Set<CClassType> blacklist) {
		if(blacklist.contains(c)) {
			return blacklist;
		}
		blacklist.add(c);
		try {
			for(CClassType s : c.getSuperclassesForType()) {
				blacklist.add(s);
				blacklist.addAll(getAllCastableClassesWithBlacklist(s, blacklist));
			}
			for(CClassType iface : c.getInterfacesForType()) {
				blacklist.add(iface);
				blacklist.addAll(getAllCastableClassesWithBlacklist(iface, blacklist));
			}
		} catch(UnsupportedOperationException ex) {
			// This is a phantom class, which is allowed
		}
		return blacklist;
	}

	/**
	 * Returns whether or not a given MethodScript value is an instanceof the specified MethodScript type.
	 * @param value
	 * @param instanceofThis
	 * @return
	 */
	public static boolean isInstanceof(Mixed value, Class<? extends Mixed> instanceofThis) {
		FullyQualifiedClassName typeof = typeof(instanceofThis);
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
		return isInstanceof(value, instanceofThis.getFQCN());
	}

	private static FullyQualifiedClassName typeof(Class<?> c) {
		typeof type = c.getAnnotation(typeof.class);
		if(type == null) {
			return null;
		} else {
			return FullyQualifiedClassName.forFullyQualifiedClass(type.value());
		}
	}

}
