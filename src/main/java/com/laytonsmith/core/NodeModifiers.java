package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.SmartComment;

/**
 * A NodeModifiers object is a container for modifiers such as smart comments and annotations that belong to a specific
 * ParseTree node.
 */
public class NodeModifiers {

	private SmartComment comment;

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

	public SmartComment getComment() {
		return comment;
	}


}
