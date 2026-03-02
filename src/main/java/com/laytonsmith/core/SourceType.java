package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;

/**
 * A SourceType is a type that can be represented in code.
 * @author Cailin
 */
public interface SourceType {

	/**
	 * Returns this type as a LeftHandSideType.
	 * @return
	 */
	LeftHandSideType asLeftHandSideType();

	/**
	 * If and only if this was constructed in such a way that it could have been a CClassType to begin with, this
	 * function will return the CClassType. This is generally useful when converting initially from a CClassType, and
	 * then getting that value back, however, it can be used anyways if the parameters are such that it's allowed. In
	 * particular, this cannot be a type union, and the LeftHandGenericUse statement must be null. (There may be
	 * concrete generic parameters attached to the underlying CClassType though.) If these requirements are not met, a
	 * CREIllegalArgumentException is thrown.
	 *
	 * @param t
	 * @return
	 */
	CClassType asConcreteType(Target t) throws CREIllegalArgumentException;
}
