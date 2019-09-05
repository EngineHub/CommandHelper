package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.compiler.Keyword;

/**
 *
 */
@Keyword.keyword("switch_ic")
public class SwitchIcKeyword extends SimpleBlockKeywordFunction {
	@Override
	protected Integer[] getFunctionArgumentCount() {
		return new Integer[]{1};
	}

	@Override
	public String docs() {
		return "Defines a switch_ic block, which is a more efficient, but narrower version of an if/else if/else"
				+ " chain doing case insensitive comparisons.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}
}
