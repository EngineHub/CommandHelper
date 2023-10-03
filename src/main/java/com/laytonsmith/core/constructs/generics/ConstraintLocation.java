package com.laytonsmith.core.constructs.generics;

/**
 * This enum represents where in the code the Constraint type may be placed.
 */
public enum ConstraintLocation {
	/**
	 * The definition point, that is, the <code>&lt;T&gt;</code> in <code>class A&lt;T&gt;</code> or
	 * <code>&gt;T&lt; T method(){...}</code>
	 */
	DEFINITION("generic type definition"),
	/**
	 * The left hand side of a statement, either the left hand of an assignment, or the parameter definition
	 * of a function call. This can be a bit confusing sometimes, because you can have a left hand side value
	 * on the right hand side, particularly during typechecking, where the concrete runtime values may not be
	 * known.
	 */
	LHS("left hand type definition"),
	/**
	 * The right hand side of a statement, either the right hand of an assignment, or the parameter sent to
	 * a function.
	 */
	RHS("right hand type definition");

	/**
	 * The value here should flow with the sentence "at the location of the ..."
	 */
	private final String locationName;

	private ConstraintLocation(String locationName) {
		this.locationName = locationName;
	}

	public String getLocationName() {
		return this.locationName;
	}
}
