package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashSet;
import java.util.Set;

/**
 * This class checks "instanceof" for native MethodScript objects, unlike the java "instanceof" keyword.
 */
public class InstanceofUtil {
	/**
	 * Returns a list of all classes that the specified class can be validly cast to. This includes all super classes,
	 * as well as all interfaces (and superclasses of those interfaces, etc) and java.lang.Object, as well as the class
	 * itself.
	 *
	 * @param c The class to search for.
	 * @return
	 */
	public static Set<CClassType> getAllCastableClasses(CClassType c, Environment env) {
		Set<CClassType> ret = new HashSet<>();
		getAllCastableClassesWithBlacklist(c, ret, env);
		return ret;
	}

	/**
	 * Private version of {@link #getAllCastableClasses(java.lang.Class)}
	 *
	 * @param c
	 * @param blacklist
	 * @return
	 */
	private static Set<CClassType> getAllCastableClassesWithBlacklist(CClassType c, Set<CClassType> blacklist,
			Environment env) {
		if(blacklist.contains(c)) {
			return blacklist;
		}
		blacklist.add(c);
		try {
			for(CClassType s : c.getSuperclassesForType(env)) {
				blacklist.addAll(getAllCastableClassesWithBlacklist(s, blacklist, env));
			}
			for(CClassType iface : c.getInterfacesForType(env)) {
				blacklist.addAll(getAllCastableClassesWithBlacklist(iface, blacklist, env));
			}
		} catch (UnsupportedOperationException ex) {
			if(ClassDiscovery.GetClassAnnotation(c.getClass(), typeof.class) != null) {
				throw new RuntimeException("Unexpected UnsupportedOperationException from " + c.getName());
			}
		}
		return blacklist;
	}

	/**
	 * Returns true whether or not a given MethodScript value is an instance of the specified MethodScript type.
	 *
	 * @param value The value to check for
	 * @param instanceofThis The string type to check. This must be the fully qualified name.
	 * @param env
	 * @return
	 */
	public static boolean isInstanceof(Mixed value, FullyQualifiedClassName instanceofThis, Environment env) {
		Static.AssertNonNull(instanceofThis, "instanceofThis may not be null");
		if(instanceofThis.getFQCN().equals("auto")) {
			return true;
		}
		if(value instanceof CFunction) {
			// TODO: Need to put the return type here, so we can work with this, but for now, just always return false
			return false;
		}
		for(CClassType c : getAllCastableClasses(value.typeof(), env)) {
			FullyQualifiedClassName typeof = c.getFQCN();
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
	 * @param env
	 * @return
	 */
	public static boolean isInstanceof(Mixed value, Class<? extends Mixed> instanceofThis, Environment env) {
		FullyQualifiedClassName typeof = typeof(instanceofThis);
		return typeof == null ? false : isInstanceof(value, typeof, env);
	}

	/**
	 * Returns whether or not a given MethodScript value is an instance of the specified MethodScript type.
	 *
	 * @param value The value to check for
	 * @param instanceofThis The CClassType to check
	 * @param env
	 * @return
	 */
	public static boolean isInstanceof(Mixed value, CClassType instanceofThis, Environment env) {
		return isInstanceof(value, instanceofThis.getFQCN(), env);
	}

	private static FullyQualifiedClassName typeof(Class<? extends Mixed> c) {
		typeof type = ClassDiscovery.GetClassAnnotation(c, typeof.class);
		if(type == null) {
			return null;
		} else {
			return FullyQualifiedClassName.forNativeClass(c);
		}
	}

}
