package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.ArrayList;
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
public class LeftHandGenericUse {

	@StandardField
	private final List<Constraints> constraints;
	private final Target target;

	public LeftHandGenericUse(CClassType forType, Target t, Environment env, Constraints... constraints) {
		this.target = t;
		this.constraints = Arrays.asList(constraints);
		ConstraintValidator.ValidateLHS(t, forType, Arrays.asList(constraints), env);
	}

	@Override
	public boolean equals(Object that) {
		return ObjectHelpers.DoEquals(this, that);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	public List<Constraints> getConstraints() {
		return constraints;
	}

	/**
	 * Works like toString, but uses the class's simple name.
	 *
	 * @return
	 */
	public String toSimpleString() {
		StringBuilder b = new StringBuilder();
		boolean doComma = false;
		for(Constraints cc : constraints) {
			if(doComma) {
				b.append(", ");
			}
			doComma = true;
			b.append(cc.toSimpleString());
		}
		return b.toString();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		boolean doComma = false;
		for(Constraints cc : constraints) {
			if(doComma) {
				b.append(", ");
			}
			doComma = true;
			b.append(cc.toString());
		}
		return b.toString();
	}

	/**
	 * Checks if the generics on the RHS are within bounds of this LHS generic definition.
	 *
	 * @param env The environment
	 * @param types The types, each argument corresponding to the Constraints objects, that is, each argument
	 * to the generic parameter set.
	 * @return True if all types are within bounds.
	 */
	public boolean isWithinBounds(Environment env, LeftHandSideType... types) {
		if(this.getConstraints().size() != types.length) {
			return false;
		}
		for(int i = 0; i < this.getConstraints().size(); i++) {
			Constraints lhs = this.getConstraints().get(i);
			LeftHandSideType type = types[i];
			if(!lhs.withinBounds(type, env)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the input LHS type is a subtype of this LHS.
	 *
	 * @param env The environment
	 * @param presumedSubtype The value to check if it is within the bounds represented by this object
	 */
	public boolean isWithinBounds(Environment env, LeftHandGenericUse presumedSubtype) {
		List<Constraints> checkIfTheseConstraints = presumedSubtype.getConstraints();
		List<Constraints> areWithinBoundsOfThese = this.getConstraints();
		if(checkIfTheseConstraints.size() != areWithinBoundsOfThese.size()) {
			return false;
		}
		for(int i = 0; i < areWithinBoundsOfThese.size(); i++) {
			Constraints definition = areWithinBoundsOfThese.get(i);
			Constraints lhs = checkIfTheseConstraints.get(i);
			// Check that the LHS fits the bounds of the definition
			List<String> errors = new ArrayList<>();
			if(!definition.withinBounds(lhs, errors, env)) {
				return false;
			}
		}
		return true;
	}

}
