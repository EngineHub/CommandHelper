package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.SimpleBlockKeywordFunction;

/**
 *
 */
@Keyword.keyword("for")
public class ForKeyword extends SimpleBlockKeywordFunction {

	@Override
	protected Integer[] getFunctionArgumentCount() {
		return new Integer[]{3};
	}

}
