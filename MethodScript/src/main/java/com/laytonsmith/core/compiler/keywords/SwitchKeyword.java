package com.laytonsmith.core.compiler.keywords;

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

}
