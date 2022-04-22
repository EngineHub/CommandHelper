package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

import java.util.Set;

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
 * <p>
 * Constraints can be placed in 3 different locations.
 * <ul>
 *     <li>Definitions - That is, a class or method definition</li>
 *     <li>LHS - Function parameter definitions or LHS of an assignment</li>
 *     <li>RHS - Function parameters, RHS of an assignment</li>
 * </ul>
 *
 * Different constraints are valid in different locations, though the RHS can only contain a concrete class, and so
 * isn't grouped as part of the Constraint heirarchy itself.
 * <p>
 * For the Definition site and LHS sites though, some constraints are simply not valid at all, and others must be used
 * as a wildcard.
 * <p>
 * Note that while the class implements Comparable, the order is not well defined, and is subject to change. This is
 * an implementation detail to allow lists of Contraint objects to be ordered in a normal, deterministic way, in order
 * to be properly comparable against other lists of Constraints. The specific order should not be relied upon beyond
 * the execution of a single process.
 */
public abstract class Constraint implements Comparable<Constraint> {
	private final String typename;
	private final boolean isWildcard;
	private final Target target;

	/**
	 * Constructs a new constraint.
	 * @param constraintName The name of the constraint, such as T or ?
	 */
	protected Constraint(Target t, String constraintName) {
		this.typename = constraintName;
		this.isWildcard = this.typename.equals("?");
		this.target = t;
	}

	/**
	 * Returns the name of the type, for instance T. If defined as a wildcard, this will be <code>?</code>
	 * @return The typename
	 */
	public String getTypeName() {
		return this.typename;
	}

	/**
	 * Returns a Set which contains the locations where this is valid to be defined at.
	 */
	public abstract Set<ConstraintLocation> validLocations();

	/**
	 * This returns the name of the type of constraint, for instance "lower bound constraint". This is useful for
	 * identifying the constraint type in error messages.
	 */
	public abstract String getConstraintName();

	/**
	 * Returns true if the type was defined as a wildcard.
	 */
	public boolean isWildcard() {
		return isWildcard;
	}

	/**
	 * Returns the code target where this constraint was defined.
	 */
	public Target getTarget() {
		return target;
	}

	/**
	 * Returns true if the provided constraint is within the bounds defined by this constraint, false if it isn't.
	 * Given that class Z extends class Y which extends class X, and class Z has a public no arg constructor,
	 * consider the following class definition: <code>class C&lt;T extends X & new T()&gt;</code> and the LHS
	 * definition: <code>C&lt;? extends Y & new ?()&gt;</code> and the RHS <code>new C&lt;Z&gt;()</code>.
	 *
	 * This code is perfectly valid, however, when validating the LHS, we will compare the LHS constraint
	 * <code>new ?()</code> to
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
	 * @param env The environment.
	 * @return True if the constraint is within the bounds, false otherwise.
	 */
	public final Boolean isWithinConstraint(Constraint lhs, Environment env) {
		ConstraintToConstraintValidator validator = this.getConstraintToConstraintValidator(env);
		Boolean baseResult = false;
		if(lhs instanceof ConstructorConstraint c) {
			baseResult = validator.isWithinBounds(c);
		} else if(lhs instanceof ExactType c) {
			baseResult = validator.isWithinBounds(c);
		} else if(lhs instanceof LowerBoundConstraint c) {
			baseResult = validator.isWithinBounds(c);
		} else if(lhs instanceof UpperBoundConstraint c) {
			baseResult = validator.isWithinBounds(c);
		} else if(lhs instanceof UnboundedConstraint c) {
			baseResult = validator.isWithinBounds(c);
		} else {
			throw new Error("Unhandled constraint type");
		}
		return baseResult;

	}

	/**
	 * Returns true if the type is within the bounds of this constraint.
	 *
	 * @param type The type to check
	 * @param env The environment.
	 * @return True if the type is within the bounds of this constraint.
	 */
	public abstract boolean isWithinConstraint(LeftHandSideType type, Environment env);

	/**
	 * Returns true if type unions can be used in the LHS of this value. Note that in no case can type unions
	 * be used on the RHS, since those require a concrete type.
	 * @return True if the constraint can support type unions.
	 */
	public abstract boolean supportsTypeUnions();

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

	/**
	 * Works like toString, but uses the class's simple name.
	 * @return
	 */
	public abstract String toSimpleString();

	@Override
	public int compareTo(Constraint o) {
		// Just compare against the toString, order doesn't *really* matter, it's just so that equality checks
		// are deterministic.
		return this.toString().compareTo(o.toString());
	}

}
