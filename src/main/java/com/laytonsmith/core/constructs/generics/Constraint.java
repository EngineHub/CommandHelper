package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.Target;

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
}
