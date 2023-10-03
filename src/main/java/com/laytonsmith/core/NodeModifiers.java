package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.SmartComment;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 * A NodeModifiers object is a container for modifiers such as smart comments and annotations that belong to a specific
 * ParseTree node.
 */
public class NodeModifiers {

	// NOTE: Remember to update the merges if more fields are added here.
	private SmartComment comment;
	private GenericParameters genericParameters;

	/**
	 * Merges in NodeModifiers to this NodeModifiers object. For constant-length items, the value in this object take
	 * priority over the modifiers passed in.
	 *
	 * @param that The other NodeModifiers object to merge in. May be null, in which case this is a non-op.
	 */
	public void merge(NodeModifiers that) {
		if(that == null) {
			return;
		}
		if(this.comment == null) {
			this.comment = that.comment;
		}
		if(this.genericParameters == null) {
			this.genericParameters = that.genericParameters;
		}
	}

	/**
	 * Merges in NodeModifiers to this NodeModifiers object.For cases where the merge cannot happen, because one of the
	 * values would overwrite (rather than append) some data, a ConfigCompileException is thrown.
	 *
	 * @param that The other NodeModifiers object to merge in. May be null, in which case this is a non-op.
	 * @param t The code target, used for the exception if applicable.
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException Thrown if the merge would cause an overwrite of
	 * any data.
	 */
	public void mergeWithFailure(NodeModifiers that, Target t) throws ConfigCompileException {
		if(that == null) {
			return;
		}
		if(this.comment != null) {
			throw new ConfigCompileException("Smart comments not allowed here.", t);
		}
		if(this.genericParameters != null) {
			throw new ConfigCompileException("Generic parameters not allowed here.", t);
		}
		this.comment = that.comment;
		this.genericParameters = that.genericParameters;
	}

	/**
	 * Merges in NodeModifiers to this NodeModifiers object.For cases where the merge cannot happen, because one of the
	 * values would overwrite (rather than append) some data, a ConfigCompileException is thrown.For values that are
	 * considered "non critical", for instance comments or other data where data loss is not critical, no exception is
	 * thrown. In those non-critical cases, the value in this object takes priority over the modifiers passed in.
	 *
	 * @param that The other NodeModifiers object to merge in. May be null, in which case this is a non-op.
	 * @param t The code target, used for the exception if applicable.
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException Thrown if the merge would cause an overwrite of
	 * critical data.
	 */
	public void mergeWithCriticalFailure(NodeModifiers that, Target t) throws ConfigCompileException {
		if(that == null) {
			return;
		}
		// Non-critical fields
		if(this.comment == null) {
			this.comment = that.comment;
		}

		// Critical fields
		if(this.genericParameters != null) {
			throw new ConfigCompileException("Generic parameters not allowed here.", t);
		}
		this.genericParameters = that.genericParameters;
	}

	public void setComment(SmartComment comment) {
		this.comment = comment;
	}

	/**
	 * Gets the smart comment associated with this node.
	 *
	 * @return
	 */
	public SmartComment getComment() {
		return comment;
	}

	/**
	 * Sets the generics for this node. For instance, if this is a function, and the code is {@code func<string>()},
	 * then the generic parameter {@code string} would be associated with the func node.
	 *
	 * @param generics The generics to set.
	 */
	public void setGenerics(GenericParameters generics) {
		this.genericParameters = generics;
	}

	/**
	 * Gets the generic parameters associated with this node. These are defined on the right of the node, for instance
	 * {@code get_value<type>('asdf')}. Note that these are not validated at this stage.
	 *
	 * @return
	 */
	public GenericParameters getGenerics() {
		return this.genericParameters;
	}
}
