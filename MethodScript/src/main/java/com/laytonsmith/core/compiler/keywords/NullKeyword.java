package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@Keyword.keyword("null")
public class NullKeyword extends LiteralKeyword {

	@Override
	protected Construct getValue(Target t) {
		return CNull.GenerateCNull(t);
	}

}
