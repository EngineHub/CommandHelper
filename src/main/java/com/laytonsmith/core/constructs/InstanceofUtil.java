package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class checks "instanceof" for native MethodScript objects, unlike the java "instanceof" keyword.
 */
public class InstanceofUtil {
	/**
	 * Returns a list of all naked classes that the specified class can be validly cast to. This includes all super classes,
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
	 * Private version of {@link #getAllCastableClasses(CClassType, Environment)}
	 *
	 * @param c
	 * @param blacklist
	 * @return
	 */
	private static Set<CClassType> getAllCastableClassesWithBlacklist(CClassType c, Set<CClassType> blacklist,
			Environment env) {
		c = CClassType.getNakedClassType(c.getFQCN());
		if(blacklist.contains(c)) {
			return blacklist;
		}
		blacklist.add(c);
		try {
			for(CClassType s : c.getTypeSuperclasses(env)) {
				blacklist.addAll(getAllCastableClassesWithBlacklist(s, blacklist, env));
			}
			for(CClassType iface : c.getTypeInterfaces(env)) {
				blacklist.addAll(getAllCastableClassesWithBlacklist(iface, blacklist, env));
			}
		} catch (UnsupportedOperationException ex) {
			if(ClassDiscovery.GetClassAnnotation(c.getClass(), typeof.class) != null) {
				throw new RuntimeException("Unexpected UnsupportedOperationException from " + c.getName());
			}
		}
		return blacklist;
	}

//	/**
//	 * Returns true whether or not a given MethodScript value is an instance of the specified MethodScript type.
//	 *
//	 * @param value The value to check for
//	 * @param instanceofThis The string type to check. This must be the fully qualified name.
//	 * @param env
//	 * @return
//	 */
//	public static boolean isInstanceof(Mixed value, FullyQualifiedClassName instanceofThis, Environment env) {
//		Static.AssertNonNull(instanceofThis, "instanceofThis may not be null");
//		if(instanceofThis.getFQCN().equals("auto")) {
//			return true;
//		}
//		if(value instanceof CFunction) {
//			// TODO: Need to put the return type here, so we can work with this, but for now, just always return false
//			return false;
//		}
//		return isInstanceof(value.typeof(), instanceofThis, env);
//	}

//	/**
//	 * Returns true whether or not a given MethodScript type is an instance of the specified MethodScript type.
//	 *
//	 * @param type The type to check for
//	 * @param instanceofThis The string type to check. This must be the fully qualified name.
//	 * @param env
//	 * @return
//	 */
//	public static boolean isInstanceof(CClassType type, FullyQualifiedClassName instanceofThis, Environment env) {
//		Static.AssertNonNull(instanceofThis, "instanceofThis may not be null");
//		if(instanceofThis.getFQCN().equals("auto")) {
//			return true;
//		}
//		for(CClassType c : getAllCastableClasses(type, env)) {
//			FullyQualifiedClassName typeof = c.getFQCN();
//			if(typeof != null && typeof.equals(instanceofThis)) {
//				// Check generics, if they exist
//				return true;
//			}
//		}
//		return false;
//	}

	/**
	 * Returns whether or not a given MethodScript value is an instanceof the specified MethodScript type.
	 * @param value
	 * @param instanceofThis
	 * @param env
	 * @return
	 */
	public static boolean isInstanceof(Mixed value, Class<? extends Mixed> instanceofThis, Environment env) throws ClassNotFoundException {
		FullyQualifiedClassName typeof = typeof(instanceofThis);
		CClassType type = CClassType.get(typeof);
		return isInstanceof(value, type, env);
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
		return isInstanceof(value, instanceofThis, null, env);
	}

	/**
	 * Returns whether or not a given MethodScript value is an instance of the specified MethodScript type.
	 *
	 * @param value The value to check for
	 * @param instanceofThis The CClassType to check
	 * @param generics The LHS generic parameters
	 * @param env
	 * @return
	 */
	public static boolean isInstanceof(Mixed value, CClassType instanceofThis, LeftHandGenericUse generics, Environment env) {
		if(generics == null && value.typeof().getGenericParameters() == null
				&& instanceofThis.getNativeType() != null && instanceofThis.getNativeType().isAssignableFrom(value.getClass())) {
			// Short circuit this for native classes if neither side has generics, since this is faster and more memory efficient anyways
			return true;
		}
		return isInstanceof(value.typeof(), instanceofThis, generics, env);
	}

	private static final Map<CClassType, Set<CClassType>> ISINSTANCEOF_CACHE = new HashMap<>();

	/**
	 * Returns whether or not a given MethodScript type is an instance of the specified MethodScript type.
	 *
	 * @param type The type to check for
	 * @param instanceofThis The CClassType to check. This may not be null.
	 * @param generics The LHS generics definition\
	 * @param env
	 * @return
	 */
	public static boolean isInstanceof(CClassType type, CClassType instanceofThis, LeftHandGenericUse generics, Environment env) {
		Static.AssertNonNull(instanceofThis, "instanceofThis may not be null");
		instanceofThis = CClassType.getNakedClassType(instanceofThis.getFQCN());
		// Return true for AUTO, as everything can be used as AUTO.
		if(instanceofThis == CClassType.AUTO) {
			return true;
		}

		// Get cached result or compute and cache result.
		Set<CClassType> castableClasses = ISINSTANCEOF_CACHE.get(type);
		if(castableClasses == null) {
			castableClasses = getAllCastableClasses(type, env);
			ISINSTANCEOF_CACHE.put(type, castableClasses);
		}

		// Return the result.
		if(!castableClasses.contains(instanceofThis)) {
			return false;
		}

		if(instanceofThis.getGenericDeclaration() != null ) {
			return type.getGenericParameters().isInstanceof(generics, env);
		} else {
			return true;
		}
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
