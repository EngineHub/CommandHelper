package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Either;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.constraints.ExactType;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import java.util.Objects;

/**
 * A ConcreteGenericParameter is a single, actual CClassType container, which optionally may have a LeftHandGenericUse
 * attached with it.
 */
public class ConcreteGenericParameter {

	private final CClassType type;
	private final LeftHandGenericUse lhgu;
	private final Environment env;

	/**
	 * Creates a new ConcreteGenericParameter. The type may be null, representing the "none" type. If the type cannot
	 * contain parameters, but some where provided, or it can contain parameters, but not enough/too many were provided,
	 * an exception is thrown.
	 *
	 * @param type
	 * @param lhgu
	 * @param t
	 * @param env The environment.
	 * @throws CREGenericConstraintException If generic parameters were provided, but were erroneous according to the
	 * type.
	 */
	public ConcreteGenericParameter(CClassType type, LeftHandGenericUse lhgu, Target t, Environment env) {
		if(type == null && lhgu != null) {
			throw new CREGenericConstraintException("\"none\" type cannot have generic parameters.", t);
		}
		if(type != null && lhgu != null
				&& (type.getGenericDeclaration() == null || type.getGenericDeclaration().getParameterCount() < 1)) {
			throw new CREGenericConstraintException(type.getSimpleName() + " cannot contain generic parameters.", t);
		}
		if(type != null && lhgu != null) {
			ConstraintValidator.ValidateLHS(t, type, lhgu, env);
		}
		this.type = type;
		this.lhgu = lhgu;
		this.env = env;
	}

	/**
	 * Constructs a new ConcreteGenericParameter from a native type. This doesn't require a target or environment,
	 * but only works with definitely native types.
	 * @param nativeType The type.
	 * @param nativeLHGU The generic parameters for the native type.
	 * @return
	 */
	public static ConcreteGenericParameter fromNativeType(CClassType nativeType, LeftHandGenericUse nativeLHGU) {
		return new ConcreteGenericParameter(nativeType, nativeLHGU, Target.UNKNOWN, null);
	}

	/**
	 * The ConcreteGenericParameter for the {@code auto} type.
	 */
	public static final ConcreteGenericParameter AUTO = fromNativeType(Auto.TYPE, null);

	public CClassType getType() {
		return this.type;
	}

	public LeftHandGenericUse getLeftHandGenericUse() {
		if(this.type.getTypeGenericParameters() != null) {
			return this.type.getTypeGenericParameters().toLeftHandGenericUse();
		}
		return this.lhgu;
	}

	public Pair<CClassType, LeftHandGenericUse> getAsPair() {
		return new Pair<>(this.type, this.lhgu);
	}

	public LeftHandSideType asLeftHandSideType() {
		return LeftHandSideType.fromCClassType(this, Target.UNKNOWN, env);
	}

	/**
	 * Returns the individual parameter as an ExactType Constraints object.
	 * @return
	 */
	public LeftHandGenericUseParameter asLeftHandGenericUseParameter() {
		Constraints constraints = new Constraints(Target.UNKNOWN, ConstraintLocation.RHS,
				new ExactType(Target.UNKNOWN, asLeftHandSideType()));
		return new LeftHandGenericUseParameter(Either.left(constraints));
	}

	@Override
	public String toString() {
		String typeString = "none";
		if(this.type != null) {
			typeString = this.type.toString();
		}
		return typeString + (this.lhgu == null ? "" : "<" + this.lhgu.toString() + ">");
	}

	public String toSimpleString() {
		String typeString = "none";
		if(this.type != null) {
			typeString = this.type.getSimpleName();
		}
		return typeString + (this.lhgu == null ? "" : "<" + this.lhgu.toSimpleString() + ">");
	}

	@Override
	public int hashCode() {
		int hash = 7;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		final ConcreteGenericParameter other = (ConcreteGenericParameter) obj;
		if(!Objects.equals(this.type, other.type)) {
			return false;
		}
		return Objects.equals(this.lhgu, other.lhgu);
	}

}
