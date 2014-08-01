package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.SimpleBlockKeywordFunction;

/**
 *
 */
@Keyword.keyword("while")
public class WhileKeyword extends SimpleBlockKeywordFunction {

	@Override
	protected Integer[] getFunctionArgumentCount() {
		return new Integer[]{1};
	}

}
