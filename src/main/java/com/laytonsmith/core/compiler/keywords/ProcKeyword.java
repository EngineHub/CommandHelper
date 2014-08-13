package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.compiler.Keyword;

/**
 *
 */
@Keyword.keyword("proc")
public class ProcKeyword extends SimpleBlockKeywordFunction {

	@Override
	protected Integer[] getFunctionArgumentCount() {
		return null;
	}

}
