package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Either;
import com.laytonsmith.PureUtilities.Pair;
import java.util.Objects;

/**
 * A LeftHandGenericUseParameter is a container that holds a single parameter in a LeftHandGenericUse object.
 * In general, these can either be an actual value (a Constraints object) or simply a typename which must
 * be fully resolved before passing out of the defined scope.
 * <p>
 * For typenames, they contain two separate pieces of information. Their actual name (a string) and their Constraints
 * object. For validation purposes, the Constraints must fit within the definition constraints, and so those are
 * required to do validation regardless. But the typename is used for correct value alignment later. Thus, both
 * pieces of information are required for typenames.
 */
public class LeftHandGenericUseParameter {
	private final Either<Constraints, Pair<String, Constraints>> value;

	public LeftHandGenericUseParameter(Either<Constraints, Pair<String, Constraints>> value) {
		if(!value.hasLeft() && !value.hasRight()) {
			throw new Error("LeftHandGenericUseParameter must contain one or the other type, and cannot be empty");
		}
		this.value = value;
	}

	public Either<Constraints, Pair<String, Constraints>> getValue() {
		return this.value;
	}

	/**
	 * Both types of values contain constraints, and this method simplifies getting it, no matter if it's a typename
	 * or a full Constraints object.
	 * @return
	 */
	public Constraints getConstraints() {
		if(this.value.hasLeft()) {
			return this.value.getLeft().get();
		} else {
			return this.value.getRight().get().getValue();
		}
	}

	@Override
	public String toString() {
		if(value.hasLeft()) {
			return value.getLeft().get().toString();
		} else {
			return value.getRight().get().getKey();
		}
	}

	public String toSimpleString() {
		if(value.hasLeft()) {
			return value.getLeft().get().toSimpleString();
		} else {
			return value.getRight().get().getKey();
		}
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + Objects.hashCode(this.value);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		final LeftHandGenericUseParameter other = (LeftHandGenericUseParameter) obj;
		return Objects.equals(this.value, other.value);
	}



}
