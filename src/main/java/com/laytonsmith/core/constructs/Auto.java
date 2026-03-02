package com.laytonsmith.core.constructs;

import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 * Auto is not a real type, but we need a placeholder for a few meta things, such as the CClassType for it.
 *
 * @author cailin
 */
public class Auto {

	public static final CClassType TYPE = CClassType.AUTO;
	public static final LeftHandSideType LHSTYPE = TYPE.asLeftHandSideType();
	public static final LeftHandSideType LHSTYPE_VARIADIC;
	static {
		try {
			LHSTYPE_VARIADIC = LHSTYPE.asVariadicType(Target.UNKNOWN, null);
		} catch(ConfigCompileException ex) {
			throw new Error();
		}
	}
}
