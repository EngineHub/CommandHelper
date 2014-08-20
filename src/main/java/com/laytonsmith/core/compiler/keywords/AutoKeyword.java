package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@Keyword.keyword("auto")
public class AutoKeyword extends LiteralKeyword {

	@Override
	protected Construct getValue(Target t) {
		return CClassType.AUTO;
	}

}
