package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.compiler.Keyword;

/**
 * A handler that changes synchronized(arg) { code } format to synchronized(arg, code).
 * @author Pieter12345
 */
@Keyword.keyword("synchronized")
public class SynchronizedKeyword extends SimpleBlockKeywordFunction {
	
	@Override
	protected Integer[] getFunctionArgumentCount() {
		return new Integer[]{1};
	}
	
}
