package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.annotations.typeof;
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
	 * Returns a list of all naked classes that the specified class can be validly cast to.This includes all super
	 * classes, as well as all interfaces (and superclasses of those interfaces, etc) and java.lang.Object, as well as
	 * the class itself.
	 *
	 * @param c The class to search for.
	 * @param env The environment.
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
		c = CClassType.getNakedClassType(c.getFQCN(), env);
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
		} catch(UnsupportedOperationException ex) {
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
//		return isInstanceof(value.typeof(env), instanceofThis, env);
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
	 *
	 * @param value
	 * @param instanceofThis
	 * @param env
	 * @return
	 */
	public static boolean isInstanceof(Mixed value, Class<? extends Mixed> instanceofThis, Environment env)
			throws ClassNotFoundException {
		CClassType type = CClassType.get(instanceofThis);
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
		if(generics == null && value.typeof(env).getGenericParameters() == null
				&& instanceofThis.getNativeType() != null && instanceofThis.getNativeType().isAssignableFrom(value.getClass())) {
			// Short circuit this for native classes if neither side has generics, since this is faster and more memory efficient anyways
			return true;
		}
		return isInstanceof(value.typeof(env), instanceofThis, generics, env);
	}

	/**
	 * Returns whether or not a given MethodScript type is an instance of the specified MethodScript type. The following
	 * rules apply in the given order:
	 * <ul>
	 * <li>If instanceofThis == Java null, {@code true} is returned.</li>
	 * <li>If instanceofThis.equals(type) where the generic declaration of instanceofThis is absent or the generic
	 * parameters of type are instanceof the given instanceofThisGenerics, {@code true} is returned.</li>
	 * <li>Java null is only instanceof Java null.</li>
	 * <li>auto is instanceof any type.</li>
	 * <li>null is never instanceof any type. (See
	 * {@link #isAssignableTo(CClassType, CClassType, LeftHandGenericUse, Environment)} if you're looking for the
	 * assignment rules instead, where this returns true in general)</li>
	 * <li>Any type is instanceof auto.</li>
	 * <li>Nothing is instanceof void and null.</li>
	 * <li>void is instanceof nothing.</li>
	 * <li>{@code A<B>} is instanceof {@code A}, because {@code A} is {@code A<auto>}.</li>
	 * </ul>
	 *
	 * @param type - The type to check for. Java {@code null} can be used to indicate no type (e.g. from control flow
	 * breaking statements).
	 * @param instanceofThis - The {@link CClassType} to check against. Java {@code null} can be used to indicate that
	 * anything is allowed to match this (i.e. making this method return {@code true}).
	 * @param instanceofThisGenerics The LHS generics. Confusingly, this is actually on the RHS of the instanceof
	 * statement, because we generally accept LHS statements RHS of the instanceof. For example
	 * {@code (new A<int>()) instanceof A<? extends primitive>} and {@code (new A<int>()) instanceof A<int>} are both
	 * valid. In the second example, this is simply a LeftHandGenericUse with an ExactType value.
	 * @param env
	 * @return {@code true} if type is instance of instanceofThis.
	 */
	public static boolean isInstanceof(
			CClassType type, CClassType instanceofThis, LeftHandGenericUse instanceofThisGenerics, Environment env) {
		instanceofThis = (instanceofThis != null ? CClassType.getNakedClassType(instanceofThis.getFQCN(), env) : null);

		// Handle special cases.
		if((type == instanceofThis && instanceofThisGenerics == null) // Identity short circuit
				|| instanceofThis == null // java null on RHS defined as true for implementation purposes
				|| (instanceofThis.equals(type) && instanceofThis.getGenericDeclaration() == null) // no generics involved, and the types are equal
				|| CClassType.AUTO.equals(type) // auto type on
				|| CClassType.AUTO.equals(instanceofThis) // either side
				) {
			return true;
		}
		if(type == null // type is java null defined as false (except if instanceofThis was true, which is caught above)
				|| CVoid.TYPE.equals(type) // void is not instanceof anything
				|| CVoid.TYPE.equals(instanceofThis) // nothing is instanceof void
				|| CNull.TYPE.equals(instanceofThis) // nothing is instanceof null (should be compile error)
				|| CNull.TYPE.equals(type) // type is mscript null defined as false
				) {
			return false;
		}

		/*
		In general at this point, all special cases have been handled, so the approach is to validate that the
		naked type is instanceof the specified value, and then if not, return false. If it is, we also need to
		validate that the generics match, because A<int> is instanceof A<int> but not A<string>.
		 */
		// Get cached result or compute and cache result.
		CClassType nakedType = type.getNakedType(env);
		Set<CClassType> castableClasses = ISINSTANCEOF_CACHE.get(nakedType);
		if(castableClasses == null) {
			castableClasses = getAllCastableClasses(nakedType, env);
			ISINSTANCEOF_CACHE.put(nakedType, castableClasses);
		}

		// Return the result.
		if(!castableClasses.contains(instanceofThis)) {
			return false;
		}
		// The classes match, validate generics.

		// No generics defined on the RHS, or they are defined, but the LHS doesn't provide them,
		// so implied <auto>, so they pass.
		if(instanceofThis.getGenericDeclaration() == null || instanceofThisGenerics == null) {
			return true;
		}

		// They are defined on the class, AND some were provided. If they pass this, they are instanceof, otherwise
		// they aren't.
		return type.getGenericParameters().isInstanceof(instanceofThisGenerics, env);
	}

	/**
	 * This contains only naked CClassTypes.
	 */
	private static final Map<CClassType, Set<CClassType>> ISINSTANCEOF_CACHE = new HashMap<>();

	/**
	 * This function returns true if a value of a certain type is assignable to the given type. In general, this is
	 * precisely equivalent to {@link #isInstanceof(CClassType, CClassType, LeftHandGenericUse, Environment)} except
	 * this allows for null to be assigned to any value in general. The only exception to this rule is if the type is
	 * defined with the NotNull annotation.
	 *
	 * @param type The type to check for. Java {@code null} can be used to indicate no type (e.g. from control flow
	 * breaking statements), though this is in general the wrong method to use for this type of check.
	 * @param instanceofThis The type of the variable to determine if this can be assigned.
	 * @param instanceofThisGenerics The type of the LHS to validate against.
	 * @param env
	 * @return
	 */
	public static boolean isAssignableTo(CClassType type, CClassType instanceofThis, LeftHandGenericUse instanceofThisGenerics, Environment env) {
		if(type != null && type.getNakedType(env).equals(CNull.TYPE) // TODO: Check for NotNull anntoation on instanceofThis
				) {
			return true;
		}
		return isInstanceof(type, instanceofThis, instanceofThisGenerics, env);
	}

}
