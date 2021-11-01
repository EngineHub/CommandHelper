package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.core.constructs.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@StandardField
public class GenericDeclaration {

	private final List<Constraints> constraints;

	public GenericDeclaration(Target t, Constraints... constraints) {
		this.constraints = Arrays.asList(constraints);
	}

	/**
	 * Returns a list of the Constraints objects. Each Constraints object represents a single type parameter, though
	 * itself can contain multiple individual Constraint objects.
	 * @return
	 */
	public List<Constraints> getConstraints() {
		return new ArrayList<>(constraints);
	}

	public int getParameterCount() {
		return constraints.size();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		boolean joinComma = false;
		for(Constraints c : constraints) {
			if(joinComma) {
				b.append(", ");
			}
			joinComma = true;
			boolean join = false;
			for(Constraint cc : c) {
				if(join) {
					b.append(" & ");
				}
				join = true;
				b.append(cc.toString());
			}
		}
		return b.toString();
	}

	@Override
	public boolean equals(Object that) {
		return ObjectHelpers.DoEquals(this, that);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

}
