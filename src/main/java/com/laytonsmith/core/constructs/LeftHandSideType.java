package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.generics.ConstraintValidator;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
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
	 * @param types
	 * @return
	 */
	public static LeftHandSideType createTypeUnion(Target t, LeftHandSideType... types) {
		Set<Pair<CClassType, LeftHandGenericUse>> set = new HashSet<>();
		List<Boolean> isTypenameList = new ArrayList<>();
		for(LeftHandSideType union : types) {
			set.addAll(union.getTypes());
			isTypenameList.add(union.isTypeName);
		}
		return createCClassTypeUnion(t, new ArrayList<>(set), isTypenameList);
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
		return fromCClassType(type, Target.UNKNOWN);
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
	 * @return The LeftHandSideType wrapping this generic typename.
	 */
	public static LeftHandSideType fromGenericDefinitionType(GenericDeclaration declaration, String genericTypeName,
			LeftHandGenericUse genericLHGU, Target t) {
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
		return new LeftHandSideType(genericTypeName, t, null, Arrays.asList(true), genericTypeName, genericLHGU,
				constraints);
	}

	/**
	 * Creates a LeftHandSideType which
	 *
	 * @param classType The simple CClassType that this LHS represents.
	 * @param t The code target.
	 * @return A new LeftHandSideType
	 */
	public static LeftHandSideType fromCClassType(CClassType classType, Target t) {
		List<Boolean> isTypenameList = new ArrayList<>();
		isTypenameList.add(false);
		return createCClassTypeUnion(t, Arrays.asList(new Pair<>(classType, null)), isTypenameList);
	}

	/**
	 * Creates a new LeftHandSideType from the given CClassType and LeftHandGenericUse. The LeftHangGenericUse may be
	 * null if this represents a type without generics, or without generics defined.
	 *
	 * @param t The code target.
	 * @param classType The class type.
	 * @param generics The LeftHangGenericUse object.
	 * @return A new LeftHandSideType
	 */
	public static LeftHandSideType fromCClassType(CClassType classType, LeftHandGenericUse generics, Target t) {
		List<Boolean> isTypenameList = new ArrayList<>();
		isTypenameList.add(false);
		return createCClassTypeUnion(t, Arrays.asList(new Pair<>(classType, generics)), isTypenameList);
	}

	/**
	 * Creates a new LeftHandSideType from the given union of CClassTypes with no generics.
	 *
	 * @param t The code target.
	 * @param classTypes The class types.
	 * @return A new LeftHandSideType
	 */
	public static LeftHandSideType fromCClassTypeUnion(Target t, CClassType... classTypes) {
		List<Pair<CClassType, LeftHandGenericUse>> pairs = new ArrayList<>();
		List<Boolean> isTypenameList = new ArrayList<>();
		for(CClassType type : classTypes) {
			pairs.add(new Pair<>(type, null));
			isTypenameList.add(false);
		}
		return createCClassTypeUnion(t, pairs, isTypenameList);
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
	private static LeftHandSideType createCClassTypeUnion(Target t,
			List<Pair<CClassType, LeftHandGenericUse>> classTypes,
			List<Boolean> isTypenameList) {
		Objects.requireNonNull(classTypes);
		if(classTypes.isEmpty()) {
			throw new IllegalArgumentException("A LeftHandSideType object must contain at least one type");
		}

		String value = StringUtils.Join(classTypes, " | ", (pair) -> {
			if(pair.getKey() == null) {
				return "none";
			}
			String ret = pair.getKey().toString();
			if(pair.getValue() != null) {
				ret += "<";
				ret += pair.getValue().toString();
				ret += ">";
			}
			return ret;
		});
		for(Pair<CClassType, LeftHandGenericUse> classType : classTypes) {
			if(Auto.TYPE.equals(classType.getKey())) {
				if(Auto.LHSTYPE == null) {
					// Bootstrapping problem, Auto.LHSTYPE calls us, and so is null at this point
					List<Pair<CClassType, LeftHandGenericUse>> types = Arrays.asList(new Pair<>(Auto.TYPE, null));
					List<Boolean> itl = Arrays.asList(false);
					return new LeftHandSideType("auto", Target.UNKNOWN, types, itl, null, null, null);
				} else {
					return Auto.LHSTYPE;
				}
			}
		}
		return new LeftHandSideType(value, t, classTypes, isTypenameList, null, null, null);
	}

	/**
	 * Given a LeftHandSideType object {@code type} that might be a type union containing type names, resolves
	 * each component of the type union into concrete types. Typenames
	 * can only be used in their defined context, and if they need to leak beyond that, must be resolved. This usually
	 * entails taking the generic parameters for the given call site, but might also involve using the inferredType
	 * or perhaps simply returning auto. If the value passed in is not a typename, it is simply returned, so this
	 * can be used in general, without first checking if it would need to be called.
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
			Pair<CClassType, LeftHandGenericUse> type = types.getTypes().get(i);
			LeftHandSideType inferredType = inferredTypes == null
					? null : inferredTypes.get(type.getKey().getFQCN().getFQCN());
			LeftHandSideType newType = LeftHandSideType.fromCClassType(type.getKey(), type.getValue(), t);
			newType.isTypeName = types.isTypenameList.get(i);
			if(newType.isTypeName) {
				newType.genericTypeName = type.getKey().getFQCN().getFQCN();
			}
			lhst[i] = resolveTypeFromGenerics(t, env, newType, parameters, declaration, inferredType);
		}
		return LeftHandSideType.createTypeUnion(t, lhst);
	}

	/**
	 * Given a LeftHandSideType object {@code type}, resolves this into a non-typename if it is a typename. Typenames
	 * can only be used in their defined context, and if they need to leak beyond that, must be resolved. This usually
	 * entails taking the generic parameters for the given call site, but might also involve using the inferredType
	 * or perhaps simply returning auto. If the value passed in is not a typename, it is simply returned, so this
	 * can be used in general, without first checking if it would need to be called.
	 *
	 * @param t The code target.
	 * @param env The environment.
	 * @param type The type to convert (or not, if it isn't a typename)
	 * @param parameters The type parameters passed to the function.
	 * @param declaration The generic declaration for the function.
	 * @param inferredType The inferredType to use, in case the type parameter is not explicitly provided.
	 * @return
	 */
	public static LeftHandSideType resolveTypeFromGenerics(Target t, Environment env, LeftHandSideType type,
			GenericParameters parameters, GenericDeclaration declaration, LeftHandSideType inferredType) {
		if(type == null) {
			// Type is none, cannot have generics
			return type;
		}
		if(type.isTypeUnion()) {
			throw new Error("Type unions cannot be resolved as a union with this method. See the other override.");
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
					Pair<CClassType, LeftHandGenericUse> p;
					p = parameters.getParameters().get(i);
					return LeftHandSideType.fromCClassType(p.getKey(), p.getValue(), t);
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
	private final List<Pair<CClassType, LeftHandGenericUse>> types;
	private final List<Boolean> isTypenameList;

	@ObjectHelpers.StandardField
	private String genericTypeName;

	private final Constraints constraints;

	private LeftHandSideType(String value, Target t, List<Pair<CClassType, LeftHandGenericUse>> types,
			List<Boolean> isTypenameList, String genericTypeName, LeftHandGenericUse genericTypeLHGU,
			Constraints constraints) {
		super(value, ConstructType.CLASS_TYPE, t);
		this.isTypenameList = isTypenameList;
		if(types != null) {
			isTypeName = false;

			// Sort the list with TreeSet first
			Set<Pair<CClassType, LeftHandGenericUse>> tempSet = new TreeSet<>((o1, o2) -> {
				String o1Index = (o1.getKey() == null ? "none" : o1.getKey().val()) + (o1.getValue() == null ? "" : ("<" + o1.getValue().toString() + ">"));
				String o2Index = (o2.getKey() == null ? "none" : o2.getKey().val()) + (o2.getValue() == null ? "" : ("<" + o2.getValue().toString() + ">"));
				return o1Index.compareTo(o2Index);
			});
			tempSet.addAll(types);
			this.types = new ArrayList<>(tempSet);
			if(isTypeUnion()) {
				for(Pair<CClassType, LeftHandGenericUse> type : types) {
					if(Auto.TYPE.equals(type.getKey())) {
						throw new CREIllegalArgumentException("auto type cannot be used in a type union", t);
					}
				}
			}
			this.genericTypeName = null;
			this.constraints = null;
		} else {
			this.types = new ArrayList<>();
			this.types.add(new Pair<>(CClassType.getFromGenericTypeName(genericTypeName, t), genericTypeLHGU));
			isTypeName = true;
			this.genericTypeName = genericTypeName;
			this.constraints = constraints;
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
	public List<Pair<CClassType, LeftHandGenericUse>> getTypes() {
		return new ArrayList<>(types);
	}

	public boolean isExtendedBy(CClassType type, Environment env) {
		return CClassType.doesExtend(env, type, this);
	}

	public boolean doesExtend(CClassType type, Environment env) {
		return CClassType.doesExtend(env, type, this);
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
	 * @return
	 */
	public String getTypename() {
		return this.genericTypeName;
	}


	public String getSimpleName() {
		return StringUtils.Join(types, " | ", pair -> {
			CClassType type = pair.getKey();
			LeftHandGenericUse lhgu = pair.getValue();
			String ret = type.getSimpleName();
			if(lhgu != null) {
				ret += "<";
				ret += lhgu.toSimpleString();
				ret += ">";
			}
			return ret;
		});
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
			return types.get(0).getKey().getTypeInterfaces(env);
		}
		Set<CClassType> interfaces = new HashSet<>(Arrays.asList(types.get(0).getKey().getTypeInterfaces(env)));
		for(Pair<CClassType, LeftHandGenericUse> subTypes : getTypes()) {
			CClassType type = subTypes.getKey();
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
			return types.get(0).getKey().getTypeInterfaces(env);
		}
		Set<CClassType> superclasses = new HashSet<>(Arrays.asList(types.get(0).getKey().getTypeSuperclasses(env)));
		for(Pair<CClassType, LeftHandGenericUse> subTypes : getTypes()) {
			CClassType type = subTypes.getKey();
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
		if(isTypeUnion()) {
			throw new CREIllegalArgumentException(exMsg, t);
		}
		Pair<CClassType, LeftHandGenericUse> type = types.get(0);
		if(type.getValue() != null) {
			throw new CREIllegalArgumentException(exMsg, t);
		}
		return type.getKey();
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
		return CVoid.TYPE.equals(types.get(0).getKey());
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
		return CClassType.AUTO.equals(types.get(0).getKey());
	}

	public boolean isNull() {
		if(isTypeUnion()) {
			return false;
		}
		return CNull.TYPE.equals(types.get(0).getKey());
	}

	/**
	 * Returns a LeftHandGenericUse statement from this typename. Note that the environment may be null when
	 * this is used in native declarations, but otherwise must be provided.
	 * @param env The environment, or null during native signature declarations.
	 * @return An equivalent LeftHandGenericUse statement.
	 */
	public LeftHandGenericUse toLeftHandGenericUse(Environment env) {
		return new LeftHandGenericUse(CClassType.getFromGenericTypeName(getTypename(), Target.UNKNOWN), Target.UNKNOWN,
				env);
	}

	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.of(ObjectModifier.FINAL);
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

}
