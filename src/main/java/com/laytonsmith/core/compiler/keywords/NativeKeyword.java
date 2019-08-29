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
@Keyword.keyword("native")
public class NativeKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		throw new ConfigCompileException("Keyword not supported yet.", list.get(keywordPosition).getTarget());
	}

	@Override
	public String docs() {
		return "";
	}

	@Override
	public Version since() {
		return MSVersion.V0_0_0;
	}

}
