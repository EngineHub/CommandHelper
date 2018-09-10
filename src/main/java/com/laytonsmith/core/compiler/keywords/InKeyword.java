package com.laytonsmith.core.compiler.keywords;

import com.methodscript.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.List;

/**
 *
 */
@Keyword.keyword("in")
public class InKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		return keywordPosition;
	}

	@Override
	public String docs() {
		return "Used in foreach loops, used to define the array over which to loop.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}

}
