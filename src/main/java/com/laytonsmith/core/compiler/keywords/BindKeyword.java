package com.laytonsmith.core.compiler.keywords;

import com.methodscript.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.compiler.Keyword;

/**
 *
 */
@Keyword.keyword("bind")
public class BindKeyword extends SimpleBlockKeywordFunction {

	@Override
	protected Integer[] getFunctionArgumentCount() {
		return null;
	}

	@Override
	public String docs() {
		return "Binds code to trigger when a particular event occurs.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}

}
