package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.SmartComment;

/**
 * If an element is Commentable, it must be able to be associated with a {@link SmartComment} block.
 */
public interface Commentable {

	/**
	 * Returns the comment on this element.
	 * @return
	 */
	SmartComment getElementComment();
}
