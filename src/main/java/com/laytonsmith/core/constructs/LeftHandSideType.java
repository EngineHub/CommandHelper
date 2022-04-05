package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
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
		for(LeftHandSideType union : types) {
			set.addAll(union.getTypes());
		}
		return createCClassTypeUnion(t, new ArrayList<>(set));
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
		return new LeftHandSideType(genericTypeName, t, null, constraints, genericTypeName, genericLHGU);
	}

	/**
	 * Creates a LeftHandSideType which
	 *
	 * @param classType The simple CClassType that this LHS represents.
	 * @param t The code target.
	 * @return A new LeftHandSideType
	 */
	public static LeftHandSideType fromCClassType(CClassType classType, Target t) {
		return createCClassTypeUnion(t, Arrays.asList(new Pair<>(classType, null)));
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
		return createCClassTypeUnion(t, Arrays.asList(new Pair<>(classType, generics)));
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
		for(CClassType type : classTypes) {
			pairs.add(new Pair<>(type, null));
		}
		return createCClassTypeUnion(t, pairs);
	}

	/**
	 * Creates a new LeftHandSideType from the given list of CClassTypes and LeftHandGenericUse pairs. Each pair
	 * represents a single type in the type union. The LeftHangGenericUse in each pair may be null, but the CClassTypes
	 * may not, except when representing the none type.
	 *
	 * @param t The code target.
	 * @param classTypes The type union types.
	 * @return A new LeftHandSideType
	 * @throws IllegalArgumentException If the classTypes list is empty.
	 */
	public static LeftHandSideType createCClassTypeUnion(Target t, List<Pair<CClassType, LeftHandGenericUse>> classTypes) {
		Objects.requireNonNull(classTypes);
		if(classTypes.isEmpty()) {
			throw new IllegalArgumentException("A LeftHandSideType object must contain at least one type");
		}
		String value = StringUtils.Join(classTypes, " | ", (pair) -> {
			if(pair.getKey() == null) {
				return "none";
			}
			String ret = pair.getKey().getFQCN().getFQCN();
			if(pair.getValue() != null) {
				ret += "<";
				ret += pair.getValue().toString();
				ret += ">";
			}
			return ret;
		});
		return new LeftHandSideType(value, t, classTypes, null, null, null);
	}

	private final boolean isTypeName;

	@ObjectHelpers.StandardField
	private final List<Pair<CClassType, LeftHandGenericUse>> types;

	@ObjectHelpers.StandardField
	private final String genericTypeName;

	private final Constraints constraints;

	private LeftHandSideType(String value, Target t, List<Pair<CClassType, LeftHandGenericUse>> types,
			Constraints constraints, String genericTypeName, LeftHandGenericUse genericTypeLHGU) {
		super(value, ConstructType.CLASS_TYPE, t);
		if(types != null) {
			isTypeName = false;
			// Sort the list with TreeSet first
			Set<Pair<CClassType, LeftHandGenericUse>> tempSet = new TreeSet<>((o1, o2) -> {
				String o1Index = o1.getKey().val() + (o1.getValue() == null ? "" : ("<" + o1.getValue().toString() + ">"));
				String o2Index = o2.getKey().val() + (o2.getValue() == null ? "" : ("<" + o2.getValue().toString() + ">"));
				return o1Index.compareTo(o2Index);
			});
			tempSet.addAll(types);
			this.types = new ArrayList<>(tempSet);
			if(isTypeUnion()) {
				for(Pair<CClassType, LeftHandGenericUse> type : types) {
					if(Auto.TYPE.equals(type.getKey())) {
						throw new CREIllegalArgumentException("auto type cannot be used in a type union", t);
					}
					if(CVoid.TYPE.equals(type.getKey())) {
						throw new CREIllegalArgumentException("void type cannot be used in a type union", t);
					}
				}
			}
			this.constraints = null;
			this.genericTypeName = null;
		} else {
			this.types = new ArrayList<>();
			this.types.add(new Pair<>(CClassType.getFromGenericTypeName(genericTypeName, t), genericTypeLHGU));
			isTypeName = true;
			this.constraints = constraints;
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
		if(!isTypeUnion()) {
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
