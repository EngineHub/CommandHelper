package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@Keyword.keyword("false")
public class FalseKeyword extends LiteralKeyword {

	@Override
	protected Construct getValue(Target t) {
		return CBoolean.GenerateCBoolean(false, t);
	}

}
