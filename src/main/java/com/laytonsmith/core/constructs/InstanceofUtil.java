package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Either;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.generics.ConcreteGenericParameter;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUseParameter;
import com.laytonsmith.core.constructs.generics.constraints.ExactType;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class checks "instanceof" for native MethodScript objects, unlike the java "instanceof" keyword.
 */
public class InstanceofUtil {

	/**
	 * Native classes never change without a JVM restart, so for classes which are native, we can put them in here
	 * instead.
	 */
	private static final Map<CClassType, Set<CClassType>> NATIVE_INSTANCEOFCACHE = new HashMap<>();

	/**
	 * Returns a list of all naked classes that the specified class can be validly cast to. This includes all super
	 * classes, as well as all interfaces (and superclasses of those interfaces, etc) and java.lang.Object, as well as
	 * the class itself.
	 *
	 * @param c The class to search for.
	 * @param env The environment.
	 * @return
	 */
	public static Set<CClassType> getAllCastableClasses(CClassType c, Environment env) {
		if(CVoid.TYPE.equals(c)) {
			return new HashSet<>(Arrays.asList(c));
		}
		if(CNull.TYPE.equals(c)) {
			return new HashSet<>();
		}
		Map<CClassType, Set<CClassType>> cache;
		if(c.getNativeType() == null) {
			Objects.requireNonNull(env);
			cache = env.getEnv(GlobalEnv.class).getIsInstanceofCache();
		} else {
			cache = NATIVE_INSTANCEOFCACHE;
		}
		c = c.getNakedType(env);
		if(!cache.containsKey(c)) {
			Set<CClassType> ret = new HashSet<>();
			getAllCastableClassesWithBlacklist(c, ret, env);
			cache.put(c, ret);
		}
		return cache.get(c);
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

	/**
	 * Returns whether or not a given MethodScript value is an instanceof the specified MethodScript type.
	 *
	 * @param value
	 * @param instanceofThis
	 * @param env
	 * @return
	 */
	public static boolean isInstanceof(Mixed value, Class<? extends Mixed> instanceofThis, Environment env) {
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
		Objects.requireNonNull(value);
		if(instanceofThis == null) {
			// "None" type, which is not instanceof anything, nor is anything instanceof it
			return false;
		}
		return isInstanceof(value, instanceofThis.asLeftHandSideType(), env);
	}

	/**
	 * Returns whether or not a given MethodScript value is an instance of the specified MethodScript type.
	 *
	 * @param value The value to check for
	 * @param type The type to check against
	 * @param env The environment
	 * @return
	 */
	@SuppressWarnings("null")
	public static boolean isInstanceof(Mixed value, LeftHandSideType type, Environment env) {
		LeftHandSideType valueType;
		if(value instanceof LeftHandSideType lhs) {
			valueType = lhs;
		} else {
			valueType = value.typeof(env).asLeftHandSideType();
		}
		return isInstanceof(valueType, type, env);
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
	 * <li>void is instanceof nothing (except void, and for implementation purposes java null).</li>
	 * <li>{@code A<B>} is instanceof {@code A}, because {@code A} is {@code A<auto>} (where there are two parts to the
	 * comparison, A is instanceof A, and B is instanceof auto).</li>
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
		// Handle special cases.
		if((type == instanceofThis && instanceofThisGenerics == null) // Identity short circuit
				|| instanceofThis == null // java null on RHS defined as true for implementation purposes
				|| (instanceofThis.equals(type) && instanceofThis.getGenericDeclaration() == null) // no generics involved, and the types are equal
				|| CClassType.AUTO.equals(type) // auto type on
				|| CClassType.AUTO.equals(instanceofThis) // either side
				) {
			return true;
		}
		if(type == null // type is java null defined as false (except if instanceofThis was null, which is caught above)
				|| CVoid.TYPE.equals(type) // void is not instanceof anything
				|| CVoid.TYPE.equals(instanceofThis) // nothing is instanceof void
				|| CNull.TYPE.equals(instanceofThis) // nothing is instanceof null (should be compile error)
				|| CNull.TYPE.equals(type) // type is mscript null defined as false
				) {
			return false;
		}
		return isInstanceof(type.asLeftHandSideType(), new ConcreteGenericParameter(instanceofThis,
				instanceofThisGenerics, Target.UNKNOWN, env).asLeftHandSideType(), env);

//
//		/*
//		In general at this point, all special cases have been handled, so the approach is to validate that the
//		naked type is instanceof the specified value, and then if not, return false. If it is, we also need to
//		validate that the generics match, because A<int> is instanceof A<int> but not A<string>.
//		 */
//		// Get cached result or compute and cache result.
//		CClassType nakedType = type.getNakedType(env);
//		Set<CClassType> castableClasses = getAllCastableClasses(nakedType, env);
//
//		// Return the result.
//		if(!castableClasses.contains(instanceofThis.getNakedType(env))) {
//			return false;
//		}
//		// The classes match, validate generics.
//
//		if(instanceofThis.getGenericDeclaration() != null && instanceofThisGenerics == null) {
//			// Pull up the actual class's generics
//			instanceofThisGenerics = instanceofThis.getTypeGenericParameters()
//					.toLeftHandGenericUse();
//		}
//		// No generics defined on the RHS, or they are defined, but the LHS doesn't provide them,
//		// so implied <auto>, so they pass.
//		if(instanceofThis.getGenericDeclaration() == null || instanceofThisGenerics == null) {
//			return true;
//		}
//
//		// They are defined on the class, AND some were provided. If they pass this, they are instanceof, otherwise
//		// they aren't.
//		return type.getTypeGenericParameters()
//				.isInstanceof(instanceofThisGenerics, env);
	}

	// TODO: Harmonize and combine these two methods, and put the CClassType one in terms of the LHS one.
	/**
	 * Returns true if the class being checked is within bounds of the specified super class and is thus assignable to
	 * the specified type. Note that for type unions, the rule is that if ALL of the classes to check extend the super
	 * class, then the check passes, and if not, even if some of them to extend the superclass, it will return false.
	 * <p>
	 * For instance, consider {@code string | int}. This extends {@code primitive}, but not {@code number}, as the value
	 * could hold a string, which doesn't extend number.
	 * <p>
	 * When considering the reverse, it returns true if the checked class extends ANY of the super classes. That is,
	 * {@code string} extends {@code array | primitive} because it extends primitive.
	 * <p>
	 * If any of the values contain a generic, those are checked via the normal generic inheritance rules.
	 *
	 * @param env The environment object.
	 * @param checkClasses The assumed "subclass" type union. Note that if this is null, it always returns false. If you
	 * mean to check for the equivalent of a null CClassType, create a type union with a null value in it, rather than
	 * sending null for the LHSType.
	 * @param superClasses The assumed "superclass" type union. Note that if this is null, it always returns false. If
	 * you mean to check for the equivalent of a null CClassType, create a type union with a null value in it, rather
	 * than sending null for the LHSType.
	 * @return
	 */
	@SuppressWarnings("null")
	public static boolean isInstanceof(LeftHandSideType checkClasses, LeftHandSideType superClasses, Environment env) {
		// This method is called during JVM bootstrapping, and we have a circular dependency between auto and
		// the regular implementation of this method. Therefore, we have a special bootstrapping modes here which
		// bypasses the code below.
		if(checkClasses == null || superClasses == null) {
			return false;
		}
		if("auto".equals(checkClasses.getTypes().get(0).getType() == null
				? "" : checkClasses.getTypes().get(0).getType().getFQCN().getFQCN())) {
			return true;
		}
		if("auto".equals(superClasses.getTypes().get(0).getType() == null
				? "" : superClasses.getTypes().get(0).getType().getFQCN().getFQCN())) {
			return true;
		}
		if(superClasses.getTypes().get(0).getType() == null) {
			return true;
		}
		if(checkClasses.getTypes().get(0).getType() == null) {
			return false;
		}

		// Handle special cases.
		if((checkClasses == superClasses) // Identity short circuit
				|| superClasses == null // java null on RHS defined as true for implementation purposes
				|| superClasses.equals(checkClasses)
				|| (checkClasses != null && checkClasses.isAuto()) // auto type on
				|| superClasses.isAuto() // either side
				) {
			return true;
		}
		if(checkClasses == null // type is java null defined as false (except if instanceofThis was null, which is caught above)
				|| superClasses.isVoid() // nothing is instanceof void
				|| superClasses.isNull() // nothing is instanceof null (should be compile error)
				|| checkClasses.isNull() // type is mscript null defined as false
				) {
			return false;
		}

		for(ConcreteGenericParameter checkClass : checkClasses.getTypes()) {
			boolean anyExtend = false;
			CClassType checkClassType = checkClass.getType();
			Set<CClassType> castableClasses = null;
			if(checkClassType.getNativeType() == null) {
				castableClasses = getAllCastableClasses(checkClassType, env);
			}
			LeftHandGenericUse checkLHGU = checkClass.getLeftHandGenericUse();
			if(checkLHGU == null) {
				// Check if the actual type has parameters
				if(checkClassType.getGenericParameters() != null) {
					checkLHGU = checkClassType.getGenericParameters()
							.toLeftHandEquivalent(checkClassType, env);
				}
			}
			if(checkLHGU == null
					&& checkClassType.getGenericDeclaration() != null
					&& checkClassType.getGenericDeclaration().getParameterCount() > 0) {
				// Synthesize it from autos
				List<LeftHandGenericUseParameter> params = new ArrayList<>();
				for(Constraints c : checkClassType.getGenericDeclaration().getConstraints()) {
					params.add(new LeftHandGenericUseParameter(Either.left(
							new Constraints(Target.UNKNOWN, ConstraintLocation.RHS,
									new ExactType(Target.UNKNOWN, Auto.LHSTYPE)))));
				}
				checkLHGU = new LeftHandGenericUse(checkClassType, Target.UNKNOWN, env, params);
			}
			for(ConcreteGenericParameter superClass : superClasses.getTypes()) {
				CClassType superClassType = superClass.getType();
				LeftHandGenericUse superLHGU = superClass.getLeftHandGenericUse();
				if(superLHGU == null && superClass.getType().getTypeGenericParameters() != null) {
					superLHGU = superClass.getType().getTypeGenericParameters().toLeftHandGenericUse();
				}
				if(superLHGU == null
						&& superClassType.getGenericDeclaration() != null
						&& superClassType.getGenericDeclaration().getParameterCount() > 0) {
					// Synthesize it from autos
					List<LeftHandGenericUseParameter> params = new ArrayList<>();
					for(Constraints c : superClassType.getGenericDeclaration().getConstraints()) {
						params.add(new LeftHandGenericUseParameter(Either.left(
								new Constraints(Target.UNKNOWN, ConstraintLocation.RHS,
										new ExactType(Target.UNKNOWN, Auto.LHSTYPE)))));
					}
					superLHGU = new LeftHandGenericUse(superClassType, Target.UNKNOWN, env, params);
				}

				// Check if check extends super, if so, set anyExtend to true and break
				if(checkClassType.equals(superClassType) && checkLHGU == null && superLHGU == null) {
					// more efficient check
					anyExtend = true;
					break;
				}

				if(checkClassType.getNativeType() != null && superClassType.getNativeType() != null
						&& checkLHGU == null && superLHGU == null) {
					// Since native classes are not allowed to extend multiple superclasees, but
					// in general, they are allowed to advertise that they do, for the sake of
					// methodscript, this can only be used to return true. If it returns true, it
					// definitely is, but if it returns false, that does not explicitly mean that
					// it doesn't. However, this check is faster, so we can do it and in 99% of
					// cases get a performance boost.
					if(superClassType.getNativeType().isAssignableFrom(checkClassType.getNativeType())) {
						anyExtend = true;
						break;
					}
				}

				if(castableClasses == null) {
					castableClasses = getAllCastableClasses(checkClassType, env);
				}

				if(!castableClasses.contains(superClassType.getNakedType(env))) {
					continue;
				}

				if(checkLHGU == null && superLHGU == null) {
					anyExtend = true;
					break;
				}
				// At this point, the types match or are castable, but we also need to consider the generics.
				// They may not be the same type. We need to convert the generic parameters into the right supertype
				// and then compare. For instance, if class A<T, U> extends B<U>, then we need to convert <T, U>
				// into <U>, then compare against superLHGU. But actually, some classes might not have generics
				// at all, so it's just up to each class to tell us the parameters.
				checkLHGU = checkClassType.getSuperclassGenerics(superClass.getType(), checkLHGU, env);
				if((superLHGU == null && checkLHGU == null)
						|| (superLHGU != null && superLHGU.isWithinBounds(env, checkLHGU))) {
					anyExtend = true;
					break;
				}

			}
			if(!anyExtend) {
				// All of the check classes must extend any of the super classes.
				return false;
			}
		}
		return true;
	}

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
		return isAssignableTo(type == null ? null : type.asLeftHandSideType(),
				LeftHandSideType.fromCClassType(
						new ConcreteGenericParameter(instanceofThis, instanceofThisGenerics, Target.UNKNOWN, env), Target.UNKNOWN, env), env);
	}

	/**
	 * This function returns true if a value of a certain type is assignable to the given type. In general, this is
	 * precisely equivalent to {@link #isInstanceof(CClassType, CClassType, LeftHandGenericUse, Environment)} except
	 * this allows for null to be assigned to any value in general. The only exception to this rule is if the type is
	 * defined with the NotNull annotation, or the type itself is NotNull.
	 * <p>
	 * Additionally, if the type is a non-type union {@code void}, and the instanceofThis value is not a type union or
	 * auto, then this always returns false, since this condition doesn't make practical sense, as void is not really an
	 * actionable value. (Type unions containing void are possible, however, and it's possible to assign void to auto).
	 *
	 * @param type The type to check for. Java {@code null} can be used to indicate no type (e.g. from control flow
	 * breaking statements), though this is in general the wrong method to use for this type of check.
	 * @param instanceofThis The type of the variable to determine if this can be assigned.
	 * @param env
	 * @return
	 */
	public static boolean isAssignableTo(LeftHandSideType type, LeftHandSideType instanceofThis, Environment env) {
		if(type != null && !type.isTypeUnion()) {
			// Only one iteration here
			for(ConcreteGenericParameter t : type.getTypes()) {
				// TODO: Check for NotNull anntoation on instanceofThis
				if(t.getType() != null) {
					if(CNull.TYPE.equals(t.getType().getNakedType(env))) {
						return true;
					}
				}
			}
		}

		if(type != null && type.isVoid() && !instanceofThis.isAuto() && !instanceofThis.isTypeUnion()) {
			return false;
		}

		return isInstanceof(type, instanceofThis, env);
	}

}
