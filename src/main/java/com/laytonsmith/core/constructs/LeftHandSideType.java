package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Either;
import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.generics.ConcreteGenericParameter;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.ConstraintValidator;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUseParameter;
import com.laytonsmith.core.constructs.generics.constraints.ExactTypeConstraint;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.ObjectModifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * A LeftHandSideType is a type reference that belongs on the left hand side (LHS) of a value definition. This is
 * opposed to a right hand side (RHS) type, which is generally represented with CClassType. A LHS type is different in
 * the sense that, while it may contain a concrete, instantiatable type, it does not necessarily have to. For instance,
 * in the definition {@code string | int @x = 42;}, the LHS type is "string | int", meaning that the variable @x may
 * validly contain an instance of either a string or an int, but 42 is ONLY an int (and the specific value and type may
 * only be known at runtime anyways.) Therefore, in places where the type being represented is a LHS value, a different
 * container must be used rather than the standard CClassType. While in many cases, this may look exactly like a simple
 * RHS value, the semantics are different, and in less common cases, the rules are substantially different. For
 * instance, the generics on the LHS can contain a declaration such as {@code array<? extends number>}, but having such
 * a value on the RHS is not legal.
 * <p>
 * A LHS value is used in 3 different locations: the left hand of a variable declaration, parameter definitions in
 * callables such as procs and closures, and the return type of a callable. RHS values are used in for instance a
 * {@code new} instantiation, and are represented with CClassType.
 * <p>
 * While less common use cases exist such that a CClassType can't represent the LHS, the common use case can, for
 * instance {@code int @i = 1;}. Therefore, there are convenience methods for easily converting a CClassType into a
 * LeftHandSideType. Additionally, all the actual types represented are naked CClassType values.
 */
public final class LeftHandSideType extends Construct {

	/**
	 * Merges the inputs to create a single type union class.For instance, if {@code int | string} and
	 * {@code array | string} are passed in, the resulting type would be {@code int | string | array}. Note that for
	 * subtypes with generic parameters, these are not merged unless they are completely equal.
	 *
	 * @param t
	 * @param env
	 * @param types
	 * @return
	 */
	public static LeftHandSideType fromTypeUnion(Target t, Environment env, LeftHandSideType... types) {
		Set<ConcreteGenericParameter> set = new HashSet<>();
		List<Boolean> isTypenameList = new ArrayList<>();
		for(LeftHandSideType union : types) {
			set.addAll(union.getTypes());
			isTypenameList.add(union.isTypeName);
		}
		return createCClassTypeUnion(t, env, new ArrayList<>(set), isTypenameList);
	}

	/**
	 * Merges the inputs to create a single type union class.For instance, if {@code int | string} and
	 * {@code array | string} are passed in, the resulting type would be {@code int | string | array}. Note that for
	 * subtypes with generic parameters, these are not merged unless they are completely equal. This can only be
	 * used for types that exclusively represent native types.
	 * @param types
	 * @return
	 */
	public static LeftHandSideType fromNativeTypeUnion(LeftHandSideType... types) {
		return fromTypeUnion(Target.UNKNOWN, null, types);
	}

	/**
	 * Creates a LeftHandSideType based on the specified CClassType. This is only suitable for hardcoded types, such as
	 * {@code CString.TYPE}, and never for user input! With user input, it's important to specify the code target. Only
	 * use this version if you would have otherwise used {@code Target.UNKNOWN}.
	 *
	 * @param type The simple CClassType that this LHS represents
	 * @return A new LeftHandSideType
	 */
	public static LeftHandSideType fromHardCodedType(CClassType type) {
		return fromCClassType(type, Target.UNKNOWN, null);
	}

	/**
	 * When creating a type in a GenericDeclaration object, the type reference doesn't actually exist, but it still
	 * needs to be represented as a left hand type.For instance, if we have {@code class C<T>}, then within the class,
	 * {@code T} can be used as method return types, parameter types, field types, etc.This is not a "real" type in the
	 * sense that it can be used at runtime, but within the compilation system, this needs to be representable somehow,
	 * without actually using CClassType.Note that the relevant GenericDeclaration needs to be passed in as well. It is
	 * validated that such a type parameter exists, and it also is used to pull out the appropriate constraints so that
	 * additional validation can be done elsewhere.
	 *
	 * @param declaration The declaration which contains this type name.
	 * @param genericTypeName The generic type name, for instance {@code T}
	 * @param genericLHGU The generic parameters for this type, for instance {@code ? extends int} in
	 * {@code T<? extends int>}
	 * @param t The code target
	 * @param env The environment.
	 * @return The LeftHandSideType wrapping this generic typename.
	 * @throws IllegalArgumentException If the GenericDeclaration does not contain a Constraints value with the
	 * specified typename.
	 */
	public static LeftHandSideType fromGenericDefinitionType(GenericDeclaration declaration, String genericTypeName,
			LeftHandGenericUse genericLHGU, Target t, Environment env) throws IllegalArgumentException {
		Constraints constraints = null;
		for(Constraints c : declaration.getConstraints()) {
			if(c.getTypeName().equals(genericTypeName)) {
				constraints = c;
				break;
			}
		}
		if(constraints == null) {
			throw new IllegalArgumentException("Provided GenericDeclaration does not contain the specified type name.");
		}
		return new LeftHandSideType(genericTypeName, t, env, null, Arrays.asList(true), genericTypeName, genericLHGU);
	}

	/**
	 * When creating a type in a GenericDeclaration object, the type reference doesn't actually exist, but it still
	 * needs to be represented as a left hand type.For instance, if we have {@code class C<T>}, then within the class,
	 * {@code T} can be used as method return types, parameter types, field types, etc.This is not a "real" type in the
	 * sense that it can be used at runtime, but within the compilation system, this needs to be representable somehow,
	 * without actually using CClassType.Note that the relevant GenericDeclaration needs to be passed in as well. It is
	 * validated that such a type parameter exists, and it also is used to pull out the appropriate constraints so that
	 * additional validation can be done elsewhere.
	 *
	 * @param declaration The declaration which contains this type name.
	 * @param genericTypeName The generic type name, for instance {@code T}
	 * @param genericLHGU The generic parameters for this type, for instance {@code ? extends int} in
	 * {@code T<? extends int>}
	 * @param t The code target
	 * @param env The environment.
	 * @return The LeftHandSideType wrapping this generic typename.
	 * @throws IllegalArgumentException If the GenericDeclaration does not contain a Constraints value with the
	 * specified typename.
	 */
	public static LeftHandSideType fromNativeGenericDefinitionType(GenericDeclaration declaration, String genericTypeName,
			LeftHandGenericUse genericLHGU) throws IllegalArgumentException {
		return fromGenericDefinitionType(declaration, genericTypeName, genericLHGU, Target.UNKNOWN, null);
	}

	/**
	 * Creates a LeftHandSideType which contains no generics.
	 *
	 * @param classType The simple CClassType that this LHS represents.
	 * @param t The code target.
	 * @param env The environment.
	 * @return A new LeftHandSideType
	 */
	public static LeftHandSideType fromCClassType(CClassType classType, Target t, Environment env) {
		List<Boolean> isTypenameList = new ArrayList<>();
		isTypenameList.add(false);
		return createCClassTypeUnion(t, env, Arrays.asList(
				new ConcreteGenericParameter(classType, null, Target.UNKNOWN, null)), isTypenameList);
	}

	/**
	 * Creates a new LeftHandSideType from the given ConcreteGenericParameter.
	 *
	 * @param type The class type.
	 * @param t The code target.
	 * @param env
	 * @return A new LeftHandSideType
	 */
	public static LeftHandSideType fromCClassType(ConcreteGenericParameter type, Target t, Environment env) {
		List<Boolean> isTypenameList = new ArrayList<>();
		isTypenameList.add(false);
		return createCClassTypeUnion(t, env, Arrays.asList(type), isTypenameList);
	}

	/**
	 * Creates a new LeftHandSideType from the given native class.
	 *
	 * @param nativeClass The class type.
	 * @param generics The value returned from {@link #toNativeLeftHandGenericUse()}. The native class and
	 * position will be passed in for you.
	 * @return A new LeftHandSideType
	 */
	public static LeftHandSideType fromNativeCClassType(CClassType nativeClass, Renderer... generics) {
		LeftHandGenericUseParameter[] rendered = new LeftHandGenericUseParameter[generics.length];
		for(int i = 0; i < rendered.length; i++) {
			rendered[i] = generics[i].render(nativeClass, i);
		}
		return fromCClassType(new ConcreteGenericParameter(nativeClass,
				LeftHandGenericUse.forNativeParameters(nativeClass, rendered), Target.UNKNOWN, null), Target.UNKNOWN, null);
	}

	/**
	 * Creates a new LeftHandSideType from the given native types.
	 *
	 * @param type The native class type.
	 * @param lhgu The LeftHandGenericUse for this type, which contains only native classes.
	 * @return A new LeftHandSideType
	 */
	public static LeftHandSideType fromNativeCClassType(CClassType type, LeftHandGenericUse lhgu) {
		return fromCClassType(new ConcreteGenericParameter(type, lhgu, Target.UNKNOWN, null), Target.UNKNOWN, null);
	}

	/**
	 * Creates a new LeftHandSideType from the given union of CClassTypes with no generics.
	 *
	 * @param t The code target.
	 * @param env The environment.
	 * @param classTypes The class types.
	 * @return A new LeftHandSideType
	 */
	public static LeftHandSideType fromCClassTypeUnion(Target t, Environment env, CClassType... classTypes) {
		List<ConcreteGenericParameter> pairs = new ArrayList<>();
		List<Boolean> isTypenameList = new ArrayList<>();
		List<CClassType> tempTypes = new ArrayList<>(Arrays.asList(classTypes));
		// `int | primitive` is just `primitive`, so walk through the list, and for each type that extends
		// another type, remove it.
		Iterator<CClassType> it = tempTypes.iterator();
		while(it.hasNext()) {
			boolean remove = false;
			CClassType subType = it.next();
			for(CClassType superType : tempTypes) {
				if(subType == superType) {
					continue;
				}
				if(InstanceofUtil.isInstanceof(subType.asLeftHandSideType(), superType.asLeftHandSideType(), env)) {
					remove = true;
					break;
				}
			}
			if(remove) {
				it.remove();
			}
		}
		for(CClassType type : tempTypes) {
			pairs.add(new ConcreteGenericParameter(type, null, t, env));
			isTypenameList.add(false);
		}
		return createCClassTypeUnion(t, env, pairs, isTypenameList);
	}

	/**
	 * Creates a new LeftHandSideType from the given list of CClassTypes and LeftHandGenericUse pairs. Each pair
	 * represents a single type in the type union. The LeftHangGenericUse in each pair may be null, but the CClassTypes
	 * may not, except when representing the none type.
	 * <p>
	 * If any of the types in the union are {@code auto}, then simply {@code auto} is returned.
	 *
	 * @param t The code target.
	 * @param classTypes The type union types.
	 * @param isTypenameList A List of booleans representing if each value is a typename or not.
	 * @return A new LeftHandSideType
	 * @throws IllegalArgumentException If the classTypes list is empty.
	 */
	private static LeftHandSideType createCClassTypeUnion(Target t, Environment env,
			List<ConcreteGenericParameter> classTypes,
			List<Boolean> isTypenameList) {
		Objects.requireNonNull(classTypes);
		if(classTypes.isEmpty()) {
			throw new IllegalArgumentException("A LeftHandSideType object must contain at least one type");
		}

		String value = StringUtils.Join(classTypes, " | ", (pair) -> {
			if(pair.getType() == null) {
				return "none";
			}
			return pair.toString();
		});
		for(ConcreteGenericParameter classType : classTypes) {
			if(Auto.TYPE.equals(classType.getType())) {
				if(Auto.LHSTYPE == null) {
					// Bootstrapping problem, Auto.LHSTYPE calls us, and so is null at this point. So is ConcreteGenericParameter.AUTO.
					List<ConcreteGenericParameter> types = Arrays.asList(new ConcreteGenericParameter(Auto.TYPE, null, Target.UNKNOWN, null));
					List<Boolean> itl = Arrays.asList(false);
					return new LeftHandSideType("auto", Target.UNKNOWN, env, types, itl, null, null);
				} else {
					return Auto.LHSTYPE;
				}
			}
		}
		return new LeftHandSideType(value, t, env, classTypes, isTypenameList, null, null);
	}

	/**
	 * Given a LeftHandSideType object {@code type} that might be a type union containing type names, resolves each
	 * component of the type union into concrete types. Typenames can only be used in their defined context, and if they
	 * need to leak beyond that, must be resolved. This usually entails taking the generic parameters for the given call
	 * site, but might also involve using the inferredType or perhaps simply returning auto. If the value passed in is
	 * not a typename, it is simply returned, so this can be used in general, without first checking if it would need to
	 * be called.
	 *
	 * @param t The code target.
	 * @param env The environment.
	 * @param types The type to convert, which might be a type union (or not, if it isn't a typename)
	 * @param parameters The type parameters passed to the function.
	 * @param declaration The generic declaration for the function.
	 * @param inferredTypes The inferredTypes to use, in case the type parameter is not explicitly provided. The map
	 * maps from typename to inferredType.
	 * @return
	 */
	public static LeftHandSideType resolveTypeFromGenerics(Target t, Environment env, LeftHandSideType types,
			GenericParameters parameters, GenericDeclaration declaration, Map<String, LeftHandSideType> inferredTypes) {
		if(types == null) {
			// Type is none, cannot have generics
			return types;
		}
		LeftHandSideType[] lhst = new LeftHandSideType[types.getTypes().size()];
		for(int i = 0; i < lhst.length; i++) {
			ConcreteGenericParameter type = types.getTypes().get(i);
			LeftHandSideType inferredType = inferredTypes == null
					? Auto.LHSTYPE : inferredTypes.get(type.getType().getFQCN().getFQCN());
			LeftHandGenericUse lhgu = type.getLeftHandGenericUse();
			if(lhgu != null && lhgu.hasTypename()) {
				// We need to create a new LHGU object with the typenames replaced.
				List<LeftHandGenericUseParameter> newParameters = new ArrayList<>();
				int k = 0;
				for(LeftHandGenericUseParameter parameter : type.getLeftHandGenericUse().getParameters()) {
					if(parameter.getValue().hasLeft()) {
						// Just add it on
						newParameters.add(parameter);
					} else {
						// Do the typename replacement
						if(inferredType == null) {
							newParameters.add(Auto.LHSTYPE.toNativeLeftHandGenericUse(type.getType(), k));
						} else {
							newParameters.add(inferredType.toLeftHandGenericUse(type.getType(), t, env,
									ConstraintLocation.LHS, k));
						}
					}
					k++;
				}
				lhgu = new LeftHandGenericUse(type.getType(), t, env, newParameters);
			}
			LeftHandSideType newType = LeftHandSideType.fromCClassType(new ConcreteGenericParameter(
					type.getType(), lhgu, t, env), t, env);
			newType.isTypeName = types.isTypenameList.get(i);
			if(newType.isTypeName) {
				newType.genericTypeName = type.getType().getFQCN().getFQCN();
			}
			lhst[i] = resolveTypeFromGenerics(t, env, newType, parameters, declaration, inferredType);
		}
		return LeftHandSideType.fromTypeUnion(t, env, lhst);
	}

	/**
	 * Given a LeftHandSideType object {@code type}, resolves this into a non-typename if it is a typename.Typenames can
	 * only be used in their defined context, and if they need to leak beyond that, must be resolved. This usually
	 * entails taking the generic parameters for the given call site, but might also involve using the inferredType or
	 * perhaps simply returning auto. If the value passed in is not a typename, it is simply returned, so this can be
	 * used in general, without first checking if it would need to be called.
	 *
	 * @param t The code target.
	 * @param env The environment.
	 * @param type The type to convert (or not, if it isn't a typename)
	 * @param parameters The type parameters passed to the function.
	 * @param declaration The generic declaration for the function.
	 * @param inferredType The inferredType to use, in case the type parameter is not explicitly provided.
	 * @return
	 */
	@SuppressWarnings("checkstyle:localvariablename")
	public static LeftHandSideType resolveTypeFromGenerics(Target t, Environment env, LeftHandSideType type,
			GenericParameters parameters, GenericDeclaration declaration, LeftHandSideType inferredType) {
		if(type == null) {
			// Type is none, cannot have generics
			return type;
		}
		if(type.isTypeUnion()) {
			LeftHandSideType[] lhst = new LeftHandSideType[type.getTypes().size()];
			for(int i = 0; i < lhst.length; i++) {
				ConcreteGenericParameter _type = type.getTypes().get(i);
				LeftHandSideType newType = LeftHandSideType.fromCClassType(_type, t, env);
				newType.isTypeName = type.isTypenameList.get(i);
				if(newType.isTypeName) {
					newType.genericTypeName = _type.getType().getFQCN().getFQCN();
				}
				lhst[i] = resolveTypeFromGenerics(t, env, newType, parameters, declaration, inferredType);
			}
			return LeftHandSideType.fromTypeUnion(t, env, lhst);
		}
		if(!type.isTypeName()) {
			return type;
		}
		// Validate the parameters against the declaration, and then return the type of the correct parameter
		ConstraintValidator.ValidateParametersToDeclaration(t, env, parameters, declaration, inferredType);

		if(parameters == null && inferredType == null) {
			// Return auto for no type. This already passed, since ValidateParametersToDeclaration would have
			// failed if this were null and either auto or the inferred type wasn't sufficient due to the constraints.
			return Auto.LHSTYPE;
		}
		// It passes. Lookup the correct parameter based on the typename.
		String typename = type.getTypename();
		for(int i = 0; i < declaration.getParameterCount(); i++) {
			if(declaration.getConstraints().get(i).getTypeName().equals(typename)) {
				// Found it
				if(parameters != null) {
					LeftHandSideType p = parameters.getParameters().get(i);
					return p;
				} else if(inferredType != null) {
					return inferredType;
				}
			}
		}
		// Would be good to unit test for this, but this won't be able to happen generally in user
		// classes.
		throw new Error("Typename returned by native function is not in the GenericDeclaration!");
	}

	private boolean isTypeName;

	@ObjectHelpers.StandardField
	private final List<ConcreteGenericParameter> types;
	private final List<Boolean> isTypenameList;

	@ObjectHelpers.StandardField
	private String genericTypeName;

	private LeftHandSideType(String value, Target t, Environment env, List<ConcreteGenericParameter> types,
			List<Boolean> isTypenameList, String genericTypeName, LeftHandGenericUse genericTypeLHGU) {
		super(value, ConstructType.CLASS_TYPE, t);
		this.isTypenameList = isTypenameList;
		if(types != null) {
			isTypeName = false;

			// Sort the list with TreeSet first
			Set<ConcreteGenericParameter> tempSet = new TreeSet<>((o1, o2) -> {
				String o1Index = o1 == null ? "none" : o1.toString();
				String o2Index = o2 == null ? "none" : o2.toString();
				return o1Index.compareTo(o2Index);
			});
			tempSet.addAll(types);
			this.types = new ArrayList<>(tempSet);
			if(isTypeUnion()) {
				for(ConcreteGenericParameter type : types) {
					if(Auto.TYPE.equals(type.getType())) {
						throw new CREIllegalArgumentException("auto type cannot be used in a type union", t);
					}
				}
			}
			this.genericTypeName = null;
		} else {
			this.types = new ArrayList<>();
			this.types.add(new ConcreteGenericParameter(CClassType.getFromGenericTypeName(genericTypeName, t), genericTypeLHGU, t, env));
			isTypeName = true;
			this.genericTypeName = genericTypeName;
		}
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	/**
	 *
	 * @return
	 */
	public List<ConcreteGenericParameter> getTypes() {
		return new ArrayList<>(types);
	}

	public boolean isExtendedBy(CClassType type, Environment env) {
		return CClassType.doesExtend(env, type, this);
	}

	public boolean doesExtend(CClassType type, Environment env) {
		return this.doesExtend(type.asLeftHandSideType(), env);
	}

	public boolean doesExtend(LeftHandSideType type, Environment env) {
		return CClassType.doesExtend(env, this, type);
	}

	/**
	 * Returns true if this type represents a type union, that is, there is more than one class returned in the types.
	 *
	 * @return
	 */
	public boolean isTypeUnion() {
		return types.size() > 1;
	}

	/**
	 * Returns true if this represents a typename. A typename is a "fake" type that only exists in the scope of the
	 * given class or method, and cannot be resolved into a real type except by converting it based on the
	 * GenericDeclaration associated with it. Note that in general, you need to know if this is a read only or write
	 * only context, as the type returned will be different.
	 * <p>
	 * Type unions are never typenames, though they may consist only of, or partially of other type unions.
	 *
	 * @return
	 */
	public boolean isTypeName() {
		return isTypeName;
	}

	/**
	 * If this was defined as a typename, returns the typename, null otherwise.
	 *
	 * @return
	 */
	public String getTypename() {
		return this.genericTypeName;
	}

	public String getSimpleName() {
		return StringUtils.Join(types, " | ", pair -> pair.toSimpleString());
	}

	/**
	 * Returns an array of the set of interfaces that all of the underlying types implements.Note that if this is a type
	 * union, and not all types in the underlying types implement an interface, it is not included in this list.
	 *
	 * @param env
	 * @return
	 */
	public CClassType[] getTypeInterfaces(Environment env) {
		if(!isTypeUnion()) {
			return types.get(0).getType().getTypeInterfaces(env);
		}
		Set<CClassType> interfaces = new HashSet<>(Arrays.asList(types.get(0).getType().getTypeInterfaces(env)));
		for(ConcreteGenericParameter subTypes : getTypes()) {
			CClassType type = subTypes.getType();
			Iterator<CClassType> it = interfaces.iterator();
			while(it.hasNext()) {
				CClassType iface = it.next();
				if(!Arrays.asList(type.getTypeInterfaces(env)).contains(iface)) {
					it.remove();
				}
			}
		}
		return interfaces.toArray(CClassType[]::new);
	}

	/**
	 * Returns an array of the set of superclasses that all of the underlying types implements.Note that if this is a
	 * type union, and not all types in the underlying types implement a superclass, it is not included in this list.
	 *
	 * @param env
	 * @return
	 */
	public CClassType[] getTypeSuperclasses(Environment env) {
		if(!isTypeUnion()) {
			return types.get(0).getType().getTypeInterfaces(env);
		}
		Set<CClassType> superclasses = new HashSet<>(Arrays.asList(types.get(0).getType().getTypeSuperclasses(env)));
		for(ConcreteGenericParameter subTypes : getTypes()) {
			CClassType type = subTypes.getType();
			Iterator<CClassType> it = superclasses.iterator();
			while(it.hasNext()) {
				CClassType iface = it.next();
				if(!Arrays.asList(type.getTypeSuperclasses(env)).contains(iface)) {
					it.remove();
				}
			}
		}
		return superclasses.toArray(CClassType[]::new);
	}

	/**
	 * A collapsed type is the nearest super class for all subtypes in the type union. For single types, this is just
	 * this instance. Note that this is not equivalent to the original LeftHandSideType value, and is a loss of
	 * specificity. However, in some cases, a single type is necessary.
	 *
	 * @return A LeftHandSideType which is guaranteed to only contain one type, and not a type union.
	 */
	public LeftHandSideType getCollapsedType() {
		if(!isTypeUnion()) {
			return this;
		} else {
			// TODO
			throw new UnsupportedOperationException("Not yet supported");
		}
	}

	/**
	 * If and only if this was constructed in such a way that it could have been a CClassType to begin with, this
	 * function will return the CClassType. This is generally useful when converting initially from a CClassType, and
	 * then getting that value back, however, it can be used anyways if the parameters are such that it's allowed. In
	 * particular, this cannot be a type union, and the LeftHandGenericUse statement must be null. (There may be
	 * concrete generic parameters attached to the underlying CClassType though.) If these requirements are not met, a
	 * CREIllegalArgumentException is thrown.
	 *
	 * @param t
	 * @return
	 */
	public CClassType asConcreteType(Target t) throws CREIllegalArgumentException {
		String exMsg = "Cannot use the type \"" + getSimpleName() + "\" in this context.";
		ConcreteGenericParameter type = types.get(0);
		if(type.getLeftHandGenericUse() != null && !type.getLeftHandGenericUse().getConstraints().isEmpty()) {
			throw new CREIllegalArgumentException(exMsg, t);
		}
		return type.getType();
	}

	/**
	 * Returns true if the underlying type is a single type, and that type is void.
	 *
	 * @return
	 */
	public boolean isVoid() {
		if(isTypeUnion()) {
			return false;
		}
		return CVoid.TYPE.equals(types.get(0).getType());
	}

	/**
	 * Returns true if the underlying type is a single type, and that type is auto.
	 *
	 * @return
	 */
	public boolean isAuto() {
		if(isTypeUnion()) {
			return false;
		}
		return CClassType.AUTO.equals(types.get(0).getType());
	}

	public boolean isNull() {
		if(isTypeUnion()) {
			return false;
		}
		return CNull.TYPE.equals(types.get(0).getType());
	}

	/**
	 * This should ONLY be used when building native signatures, but otherwise behaves the same as
	 * {@link #toLeftHandGenericUse()}
	 *
	 * @return An equivalent LeftHandGenericUse statement.
	 */
	public LeftHandGenericUseParameter toNativeLeftHandGenericUse(CClassType forType, int parameterPosition) {
		return toLeftHandGenericUse(forType, Target.UNKNOWN, null, ConstraintLocation.LHS, parameterPosition);
	}

	public static interface Renderer {

		LeftHandGenericUseParameter render(CClassType forType, int parameterPosition);
	}

	public Renderer toNativeLeftHandGenericUse() {
		return new Renderer() {
			@Override
			public LeftHandGenericUseParameter render(CClassType forType, int parameterPosition) {
				return toNativeLeftHandGenericUse(forType, parameterPosition);
			}
		};
	}

	/**
	 * Returns a LeftHandGenericUse statement.This works with both typename, and concrete types, not including type
	 * unions.The underlying constraints is an ExactType constraint. This will not work if the underlying type cannot be
	 * converted into a concrete type.
	 *
	 * @param forType The type that will contain this LHGU.
	 * @param t The code target, for exceptions
	 * @param env The environment.
	 * @param location The location this will be used at.
	 * @return An equivalent LeftHandGenericUse statement.
	 */
	public LeftHandGenericUseParameter toLeftHandGenericUse(CClassType forType, Target t, Environment env,
			ConstraintLocation location, int parameterPosition) {
		if(isTypeName) {
			return new LeftHandGenericUseParameter(Either.right(new Pair<>(getTypename(),
					forType.getGenericDeclaration().getConstraints().get(parameterPosition))));
		} else {
			return new LeftHandGenericUseParameter(Either.left(new Constraints(t, location, new ExactTypeConstraint(t, this))));
		}
	}

	/**
	 * Returns the naked type for each type in the type union.
	 * @param t
	 * @param env
	 * @return
	 */
	public LeftHandSideType getNakedType(Target t, Environment env) {
		List<LeftHandSideType> newTypes = new ArrayList<>();
		for(ConcreteGenericParameter m : types) {
			if(m.getType() == null) {
				return null;
			} else if(m.getType().equals(Auto.TYPE)) {
				newTypes.add(Auto.LHSTYPE);
			} else {
				newTypes.add(m.getType().getNakedType(env).asLeftHandSideType());
			}
		}
		return LeftHandSideType.fromTypeUnion(t, env, newTypes.toArray(LeftHandSideType[]::new));
	}

	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.of(ObjectModifier.FINAL);
	}

	/**
	 * Returns a List of Sets of ObjectModifers for each underlying type in the union.
	 *
	 * @return
	 */
	public List<Set<ObjectModifier>> getTypeObjectModifiers() {
		List<Set<ObjectModifier>> ret = new ArrayList<>();
		for(ConcreteGenericParameter type : types) {
			ret.add(type.getType().getObjectModifiers());
		}
		return ret;
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_5;
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public String docs() {
		return "Represents a left hand side type expression. This has LHS semantics, including supporting bounded"
				+ " generics and type unions.";
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		return ObjectHelpers.DoEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	@Override
	public String toString() {
		return val();
	}

	@Override
	public GenericParameters getGenericParameters() {
		return null;
	}

}
