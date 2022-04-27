package com.laytonsmith.core.constructs.generics.constraints;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConcreteGenericParameter;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.ConstraintToConstraintValidator;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;

import java.util.EnumSet;
import java.util.Objects;

public class ExactType extends Constraint {

	@StandardField
	private final LeftHandSideType type;

	private Constraints declarationBounds;


	/**
	 * Constructs a new unbounded wildcard instance of ExactType.
	 *
	 * @param t The code target
	 * @param declarationBounds The bounds of the declaration. This is used to check if
	 * the given concrete right hand side is within bounds of the declaration. Note that
	 * this will cause an exception to be thrown if the declarationBounds type is a type union,
	 * as unbounded wildcards cannot be used against a type union.
	 * @return
	 */
	public static ExactType AsUnboundedWildcard(Target t, Constraints declarationBounds) {
		Objects.requireNonNull(declarationBounds);
		ExactType type;
		try {
			type = new ExactType(t);
		} catch(ConfigCompileException ex) {
			throw new Error(ex);
		}
		type.declarationBounds = declarationBounds;
		return type;
	}

	private ExactType(Target t) throws ConfigCompileException {
		super(t, "?");
		type = null;
	}

	/**
	 * Constructs a new ExactType constraint.
	 * @param t The target where this is being defined.
	 * @param type The type.
	 */
	public ExactType(Target t, LeftHandSideType type) {
		super(t, type.val());
		Objects.requireNonNull(type);
		this.type = type;
	}

	@Override
	public EnumSet<ConstraintLocation> validLocations() {
		return EnumSet.of(ConstraintLocation.LHS, ConstraintLocation.RHS);
	}

	@Override
	public String getConstraintName() {
		return type == null ? "?" : type.val();
	}

	@Override
	public boolean isWithinConstraint(LeftHandSideType type, Environment env) {
		if(this.type == null) {
			// It's an unbounded wildcard, we need the definition to determine.
			return this.declarationBounds.withinBounds(type, env);
		}
		// Loop through each type in the union, and see if the top value type matches, and the
		// other generics are in bounds.
		if(this.type.getTypes().size() != type.getTypes().size()) {
			return false;
		}
		// The order is deterministic across types that are equal, so given `A | B` and `B | A`, the order
		// of each value will be reformatted into the same order for both types. So we can simply iterate
		// each of these in order, and if any value isn't equal, then they aren't the same type.
		for(int i = 0; i < this.type.getTypes().size(); i++) {
			ConcreteGenericParameter theirs = type.getTypes().get(i);
			ConcreteGenericParameter ours = this.type.getTypes().get(i);
			if(!ours.getType().getNakedType(env).equals(theirs.getType().getNakedType(env))
					|| (ours.getLeftHandGenericUse() == null && theirs.getLeftHandGenericUse() != null)
					|| (ours.getLeftHandGenericUse() != null && theirs.getLeftHandGenericUse() == null)
					|| (ours.getLeftHandGenericUse() != null
						&& !ours.getLeftHandGenericUse().isWithinBounds(env, theirs.getLeftHandGenericUse()))) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected ConstraintToConstraintValidator getConstraintToConstraintValidator(Environment env) {
		return new ConstraintToConstraintValidator() {
			@Override
			public Boolean isWithinBounds(ConstructorConstraint lhs) {
				return false;
			}

			@Override
			public Boolean isWithinBounds(ExactType lhs) {
				if(ExactType.this.equals(lhs)) {
					return true;
				}
				if(ExactType.this.getType().isAuto() || lhs.getType().isAuto()) {
					return true;
				}
				if(!ExactType.this.getType().getNakedType(Target.UNKNOWN, env)
						.equals(lhs.getType().getNakedType(Target.UNKNOWN, env))) {
					return false;
				}
				// Upper type is the same now, for instance `array<? super int>` and `array<? super primitive>`, but
				// now we have to ensure that the <? super int> is in bounds of <? super primitive>.
				return InstanceofUtil.isInstanceof(lhs.getType(), ExactType.this.getType(), env);
			}

			@Override
			public Boolean isWithinBounds(LowerBoundConstraint lhs) {
				return false;
			}

			@Override
			public Boolean isWithinBounds(UpperBoundConstraint lhs) {
				return false;
			}

			@Override
			public Boolean isWithinBounds(UnboundedConstraint lhs) {
				throw new Error("Unexpected constraint combination.");
			}
		};
	}

	@Override
	public ExactType convertFromDiamond(Target t) {
		return this;
	}

	/**
	 * Returns the exact type for this constraint.Note that in the case of a wildcard "?", an ExactType constraint will
	 * be used, however, this value will be null.
	 *
	 * @return The type, or null if it was defined as a wildcard.
	 */
	public LeftHandSideType getType() {
		return type;
	}

	@Override
	public String toSimpleString() {
		return type == null ? "?" : type.getSimpleName();
	}

	@Override
	public String toString() {
		return type == null ? "?" : type.val();
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object that) {
		return ObjectHelpers.DoEquals(this, that);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	@Override
	public boolean supportsTypeUnions() {
		return false;
	}
}
