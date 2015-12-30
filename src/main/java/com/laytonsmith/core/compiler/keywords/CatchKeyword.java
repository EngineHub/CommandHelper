package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.List;

/**
 * 
 */
@Keyword.keyword("catch")
public class CatchKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		// While catch is a keyword, and it should generally be treated as such by text editors and
		// such, if a standalone catch is present, it is always an error. Since catch comes after
		// the try keyword, this is never something we have to worry about.
		throw new ConfigCompileException("Unexpected \"catch\" keyword", list.get(keywordPosition).getTarget());
	}

}
