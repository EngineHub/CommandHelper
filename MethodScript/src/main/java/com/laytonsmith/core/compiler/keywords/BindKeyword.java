package com.laytonsmith.core.compiler.keywords;

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

}
