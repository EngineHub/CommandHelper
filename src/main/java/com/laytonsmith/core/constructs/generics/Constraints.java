package com.laytonsmith.core.constructs.generics;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/**
 * A Constraints object contains the full list of Constraints for a given type parameter.
 */
public class Constraints extends AbstractList<Constraint> {

	private final List<Constraint> list;
	private final String typename;

	public Constraints(ConstraintLocation location, Constraint... constraints) {
		this.list = Arrays.asList(constraints);
		typename = ConstraintValidator.ValidateDefinition(this.list, location);
	}

	@Override
	public Constraint get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}

	/**
	 * Returns the name of the type. T for instance, or ? if this is a wildcard.
	 * @return
	 */
	public String getTypeName() {
		return typename;
	}

}
