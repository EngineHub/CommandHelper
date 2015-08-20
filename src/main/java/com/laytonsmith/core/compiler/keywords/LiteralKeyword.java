package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.List;

/**
 * A keyword that is a literal, and returns some data type directly.
 */
public abstract class LiteralKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		if(list.get(keywordPosition).getData() instanceof CLabel) {
			list.set(keywordPosition, new ParseTree(new CLabel(getValue(list.get(keywordPosition).getTarget())),
					list.get(keywordPosition).getFileOptions()));
		} else {
			list.set(keywordPosition, new ParseTree(getValue(list.get(keywordPosition).getTarget()),
					list.get(keywordPosition).getFileOptions()));
		}
		return keywordPosition;
	}

	protected abstract Construct getValue(Target t);

}
