package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.SimpleBlockKeywordFunction;

/**
 *
 */
@Keyword.keyword("closure")
public class ClosureKeyword extends SimpleBlockKeywordFunction {

	@Override
	protected Integer[] getFunctionArgumentCount() {
		return null;
	}

}
