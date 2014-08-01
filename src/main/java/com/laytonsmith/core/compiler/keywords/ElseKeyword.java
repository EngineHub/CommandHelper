package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.List;

/**
 *
 */
@Keyword.keyword("else")
public class ElseKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		// While else is a keyword, and it should generally be treated as such by text editors and
		// such, if a standalone else is present, it is always an error. Since else comes after
		// the other keywords, this is never something we have to worry about.
		throw new ConfigCompileException("Unexpected \"else\" keyword", list.get(keywordPosition).getTarget());
	}

}
