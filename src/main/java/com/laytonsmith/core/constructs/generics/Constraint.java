package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

import java.util.EnumSet;

/**
 * A Constraint is a single part of the general declaration. For instance, in the declaration
 * <code>T extends Number & new T(A, B, C)</code>, this has two constraints, the first being
 * <code>T extends Number</code> and the second being <code>new T(A, B, C)</code>. The first is an UpperBoundConstraint,
 * and the second is a ConstructorConstraint. A third type is a LowerBoundConstraint, which is for
 * constraints defined with <code>T super Number</code>, and finally, an exact constraint, which is simply
 * a ClassType.
 * <p>
 * In general, two different Constraints may be contradictory, and when used in combination, erroneous. The solver
 * for this isn't built in to the Constraint class however, since you need to know all constraints in the generic
 * to determine if the combination is erroneous. Individually, a constraint cannot itself be erroneous.
 *
 * Constraints can be placed in 3 different locations.
 * <ul>
 *     <li>Definitions - That is, a class or method definition</li>
 *     <li>LHS - Function parameter definitions or LHS of an assignment</li>
 *     <li>RHS - Function parameters, RHS of an assignment</li>
 * </ul>
 *
 * Different constraints are valid in different locations, though the RHS can only contain a concrete class, and so
 * isn't grouped as part of the Constraint heirarchy itself.
 *
 * For the Definition site and LHS sites though, some constraints are simply not valid at all, and others must be used
 * as a wildcard.
 */
public abstract class Constraint {
	private String typename;
	private boolean isWildcard;
	private Target target;

	/**
	 * Constructs a new constraint.
	 * @param constraintName The name of the constraint, such as T or ?
	 */
	protected Constraint(Target t, String constraintName) {
		this.typename = constraintName;
		isWildcard = this.typename.equals("?");
	}

	/**
	 * Returns the name of the type, for instance T. If defined as a wildcard, this will be <code>?</code>
	 * @return
	 */
	public String getTypeName() {
		return this.typename;
	}

	/**
	 * Returns an EnumSet which contains the locations where this is valid to be defined at.
	 * @return
	 */
	public abstract EnumSet<ConstraintLocation> validLocations();

	/**
	 * This returns the name of the type of constraint, for instance "lower bound constraint". This is useful for
	 * identifying the constraint type in error messages.
	 * @return
	 */
	public abstract String getConstraintName();

	/**
	 * Returns true if the type was defined as a wildcard.
	 * @return
	 */
	public boolean isWildcard() {
		return isWildcard;
	}

	/**
	 * Returns the code target where this constraint was defined.
	 * @return
	 */
	public Target getTarget() {
		return target;
	}

	/**
	 * Returns true if the provided constraint is within the bounds defined by this constraint, false if it isn't.
	 *
	 * Given that class Z extends class Y which extends class X, and class Z has a public no arg constructor,
	 * consider the following class definition: <code>class C&lt;T extends X & new T()&gt;</code> and the LHS
	 * definition: <code>C&lt;? extends Y & new ?()&gt;</code> and the RHS <code>new C&lt;Z&gt;()</code>. This code
	 * is perfectly valid, however, when validating the LHS, we will compare the LHS constraint <code>new ?()</code> to
	 * the definition constraint <code>? extends Y</code>. These don't compare at all, because we don't have enough
	 * information to compare these, however, comparing the RHS <code>Z</code> to both <code>? extends Y</code> and
	 * <code>new ?()</code>, they both do apply.
	 * <p>
	 * In cases where the comparison simply doesn't make sense, null is returned. However, this isn't necessarily
	 * sufficient to say that this is correct. Consider if the LHS had been defined as <code>C&lt;new ?()&gt;</code>.
	 * This is still lacking, because we need some constraint that matches the <code>? extends X</code> in the
	 * definition.
	 * <p>
	 * The key then, is to ensure that for each constraint in the class definition, at least one constraint on the LHS
	 * returns true, and none of them return false.
	 *
	 * @param lhs The constraint to determine if is in bounds of this constraint
	 * @return True if the constraint is within the bounds, false otherwise.
	 */
	public final Boolean isWithinConstraint(Constraint lhs, Environment env) {
		ConstraintToConstraintValidator validator = this.getConstraintToConstraintValidator(env);
		if(lhs instanceof ConstructorConstraint c) {
			return validator.isWithinBounds(c);
		} else if(lhs instanceof ExactType c) {
			return validator.isWithinBounds(c);
		} else if(lhs instanceof LowerBoundConstraint c) {
			return validator.isWithinBounds(c);
		} else if(lhs instanceof UpperBoundConstraint c) {
			return validator.isWithinBounds(c);
		} else if(lhs instanceof UnboundedConstraint c) {
			return validator.isWithinBounds(c);
		} else {
			throw new Error("Unhandled constraint type");
		}
	}

	/**
	 * Returns true if the concrete type is within the bounds of this constraint.
	 *
	 * @param type The concrete type to check
	 * @param generics Any LHS generics that were defined along with the type, null if there were none.
	 * @return
	 */
	public abstract boolean isWithinConstraint(CClassType type, LeftHandGenericUse generics, Environment env);

	@ForceImplementation
	protected abstract ConstraintToConstraintValidator getConstraintToConstraintValidator(Environment env);

	/**
	 * Given the Diamond Operator on the RHS, takes this LHS object and converts it to an ExactType object,
	 * which can be used on the RHS. Note that this only works with one constraint, multiple constraints
	 * cannot be inferred, by definition.
	 * @param t The code target
	 * @return The ExactType
	 * @throws CREGenericConstraintException If the type cannot be inferred from this Constraint
	 */
	public abstract ExactType convertFromDiamond(Target t) throws CREGenericConstraintException;

}
