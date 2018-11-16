package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.compiler.Keyword;

/**
 *
 */
@Keyword.keyword("while")
public class WhileKeyword extends SimpleBlockKeywordFunction {

	@Override
	protected Integer[] getFunctionArgumentCount() {
		return new Integer[]{1};
	}

	@Override
	public String docs() {
		return "Provides a mechanism to continue the loop until the specified condition is false.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

}
