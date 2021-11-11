package com.laytonsmith.core.constructs.generics;

/*package*/ interface ConstraintToConstraintValidator {
	/**
	 * If "this" is the class definition, then lhs is the LHS of the statement.
	 * @param lhs
	 * @return
	 */
	Boolean isWithinBounds(ConstructorConstraint lhs);
	/**
	 * If "this" is the class definition, then lhs is the LHS of the statement.
	 * @param lhs
	 * @return
	 */
	Boolean isWithinBounds(ExactType lhs);
	/**
	 * If "this" is the class definition, then lhs is the LHS of the statement.
	 * @param lhs
	 * @return
	 */
	Boolean isWithinBounds(LowerBoundConstraint lhs);
	/**
	 * If "this" is the class definition, then lhs is the LHS of the statement.
	 * @param lhs
	 * @return
	 */
	Boolean isWithinBounds(UpperBoundConstraint lhs);
	/**
	 * If "this" is the class definition, then lhs is the LHS of the statement.
	 * @param lhs
	 * @return
	 */
	Boolean isWithinBounds(UnboundedConstraint lhs);
}
