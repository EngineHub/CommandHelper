package com.laytonsmith.core.compiler.keywords;

import com.methodscript.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@Keyword.keyword("true")
public class TrueKeyword extends LiteralKeyword {

	@Override
	protected Construct getValue(Target t) {
		return CBoolean.GenerateCBoolean(true, t);
	}

	@Override
	public Version since() {
		return CHVersion.V3_0_1;
	}

}
