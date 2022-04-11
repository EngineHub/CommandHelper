package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.SmartComment;
import com.laytonsmith.core.constructs.generics.GenericParameters;

/**
 * A NodeModifiers object is a container for modifiers such as smart comments and annotations that belong to a specific
 * ParseTree node.
 */
public class NodeModifiers {

	private SmartComment comment;
	private GenericParameters genericParameters;

	/**
	 * Merges in NodeModifiers to this NodeModifiers object. For constant-length items, the value in this object
	 * take priority over the modifiers passed in.
	 * @param that The other NodeModifiers object to merge in. May be null, in which case this is a non-op.
	 */
	public void merge(NodeModifiers that) {
		if(that == null) {
			return;
		}
		if(this.comment == null) {
			this.comment = that.comment;
		}
	}

	public void setComment(SmartComment comment) {
		this.comment = comment;
	}

	/**
	 * Gets the smart comment associated with this node.
	 * @return
	 */
	public SmartComment getComment() {
		return comment;
	}

	/**
	 * Sets the generics for this node. For instance, if this is a function, and the code is {@code func<string>()},
	 * then the generic parameter {@code string} would be associated with the func node.
	 * @param generics The generics to set.
	 */
	public void setGenerics(GenericParameters generics) {
		this.genericParameters = generics;
	}

	/**
	 * Gets the generic parameters associated with this node. These are defined on the right of the node, for instance
	 * {@code get_value<type>('asdf')}. Note that these are not validated at this stage.
	 * @return
	 */
	public GenericParameters getGenerics() {
		return this.genericParameters;
	}


}
