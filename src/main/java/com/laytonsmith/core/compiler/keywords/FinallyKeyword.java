package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.List;

/**
 *
 */
@Keyword.keyword("finally")
public class FinallyKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		// While finally is a keyword, and it should generally be treated as such by text editors and
		// such, if a standalone finally is present, it is always an error. Since finally comes after
		// the try keyword, this is never something we have to worry about.
		throw new ConfigCompileException("Unexpected \"finally\" keyword", list.get(keywordPosition).getTarget());
	}

	@Override
	public String docs() {
		return "Defines a clause in a try, try/catch sequence which runs regardless of what happens"
				+ " in either the try or catch blocks.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

}
