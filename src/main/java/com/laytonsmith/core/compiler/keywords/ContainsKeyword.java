package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.ArrayHandling;
import java.util.List;

/**
 *
 */
@Keyword.keyword("contains")
public class ContainsKeyword extends Keyword {

	private final static String ARRAY_CONTAINS = new ArrayHandling.array_contains().getName();

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		if(list.get(keywordPosition).getData() instanceof CFunction){
			// It's not a keyword, it's a function
			return keywordPosition;
		}
		if(keywordPosition == 0){
			throw new ConfigCompileException("Expected array to proceed \"contains\" keyword, but no array was found.", list.get(keywordPosition).getTarget());
		}
		if(list.size() <= keywordPosition + 1){
			throw new ConfigCompileException("Expected value to follow \"contains\" keyword, but no value was found.", list.get(keywordPosition).getTarget());
		}
		ParseTree node = new ParseTree(new CFunction(ARRAY_CONTAINS, list.get(keywordPosition).getTarget()), list.get(keywordPosition).getFileOptions());
		node.addChild(list.get(keywordPosition - 1));
		node.addChild(list.get(keywordPosition + 1));
		list.set(keywordPosition - 1, node); // Overwrite the LHS (Array)
		list.remove(keywordPosition); // Remove the keyword
		list.remove(keywordPosition); // Remove the RHS (Value)
		return keywordPosition;
	}

}
