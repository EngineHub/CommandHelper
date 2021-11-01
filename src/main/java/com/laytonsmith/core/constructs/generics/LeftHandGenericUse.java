package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

import java.util.Arrays;
import java.util.List;

/**
 * This class represents the generic parameters on the LHS. In general, this can contain several types of constraints,
 * including the ExactType constraint. Unlike the RHS, this information is not required to be kept around post
 * compilation, since the RHS is typechecked against this information, and then once confirmed to be correct, is no
 * longer needed for dynamic use of the reified type. The RHS can in general contain LHS information though,
 * particularly when the class specified on the RHS itself contains generics, in which case, the information within
 * those parameters will be LHS information.
 */
@StandardField
public class LeftHandGenericUse {

	private final List<Constraints> constraints;
	private final Target target;

	public LeftHandGenericUse(CClassType type, Target t, Constraints... constraints) {
		this.target = t;
		this.constraints = Arrays.asList(constraints);
		ConstraintValidator.ValidateLHS(t, type, Arrays.asList(constraints));
	}


	@Override
	public boolean equals(Object that) {
		return ObjectHelpers.DoEquals(this, that);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	@Override
	public String toString() {
		return ObjectHelpers.DoToString(this);
	}
}
