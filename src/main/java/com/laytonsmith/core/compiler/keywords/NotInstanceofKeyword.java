package com.laytonsmith.core.compiler.keywords;

import java.util.List;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Function;

/**
 *
 */
@Keyword.keyword("notinstanceof")
public class NotInstanceofKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		if(keywordPosition == 0) {
			throw new ConfigCompileException("Expected value to proceed \"notinstanceof\" keyword, but no identifiers were found.", list.get(keywordPosition).getTarget());
		}
		if(list.size() <= keywordPosition + 1) {
			throw new ConfigCompileException("Expected type to follow \"notinstanceof\" keyword, but no type was found.", list.get(keywordPosition).getTarget());
		}
		ParseTree node = new ParseTree(new CFunction(Function.INSTANCEOF,
				list.get(keywordPosition).getTarget()), list.get(keywordPosition).getFileOptions());
		node.addChild(list.get(keywordPosition - 1));
		node.addChild(list.get(keywordPosition + 1));
		ParseTree not = new ParseTree(new CFunction(Function.NOT,
				list.get(keywordPosition).getTarget()), list.get(keywordPosition).getFileOptions());
		not.addChild(node);
		list.set(keywordPosition - 1, not); // Overwrite the LHS
		list.remove(keywordPosition); // Remove the keyword
		list.remove(keywordPosition); // Remove the RHS
		return keywordPosition;
	}

	@Override
	public String docs() {
		return "Provides the same functionality as instanceof, but returns a negated value.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

}
