package com.laytonsmith.core.constructs.generics;

/*package*/ interface ConstraintToConstraintValidator {
	Boolean isWithinBounds(ConstructorConstraint lhs);
	Boolean isWithinBounds(ExactType lhs);
	Boolean isWithinBounds(LowerBoundConstraint lhs);
	Boolean isWithinBounds(UpperBoundConstraint lhs);
	Boolean isWithinBounds(UnboundedConstraint lhs);
}
