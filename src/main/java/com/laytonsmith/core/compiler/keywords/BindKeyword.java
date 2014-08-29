package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.List;

/**
 *
 */
@Keyword.keyword("bind")
public class BindKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		SimpleBlockKeywordFunction.doProcess(getKeywordName(), null, true, list, keywordPosition);
		ParseTree node = new ParseTree(new CFunction(S, list.get(keywordPosition).getTarget()), list.get(keywordPosition).getFileOptions());
		node.addChild(list.get(keywordPosition));
		list.set(keywordPosition, node);
		return keywordPosition;
	}

}
