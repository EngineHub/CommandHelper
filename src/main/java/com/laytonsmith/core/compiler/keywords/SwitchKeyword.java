package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.compiler.Keyword;

/**
 *
 */
@Keyword.keyword("switch")
public class SwitchKeyword extends SimpleBlockKeywordFunction {

	@Override
	protected Integer[] getFunctionArgumentCount() {
		return new Integer[]{1};
	}

	@Override
	public String docs() {
		return "Defines a switch block, which is a more efficient, but narrower version of an if/else if/else chain.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}

}
