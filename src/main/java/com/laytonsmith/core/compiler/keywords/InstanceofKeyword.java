package com.laytonsmith.core.compiler.keywords;

import java.util.List;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.DataHandling._instanceof;

/**
 *
 */
@Keyword.keyword("instanceof")
public class InstanceofKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		if(list.get(keywordPosition).getData() instanceof CFunction) {
			// It's not a keyword, it's a function
			return keywordPosition;
		}
		if(keywordPosition == 0) {
			throw new ConfigCompileException("Expected value to proceed \"instanceof\" keyword, but no identifiers were found.", list.get(keywordPosition).getTarget());
		}
		if(list.size() <= keywordPosition + 1) {
			throw new ConfigCompileException("Expected type to follow \"instanceof\" keyword, but no type was found.", list.get(keywordPosition).getTarget());
		}
		ParseTree node = new ParseTree(new CFunction(
				_instanceof.NAME, list.get(keywordPosition).getTarget()), list.get(keywordPosition).getFileOptions());
		node.addChild(list.get(keywordPosition - 1));
		node.addChild(list.get(keywordPosition + 1));
		list.set(keywordPosition - 1, node); // Overwrite the LHS
		list.remove(keywordPosition); // Remove the keyword
		list.remove(keywordPosition); // Remove the RHS
		return keywordPosition;
	}

	@Override
	public String docs() {
		return "Provides a way to get whether or not the given value is an instance of the specified type.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

}
