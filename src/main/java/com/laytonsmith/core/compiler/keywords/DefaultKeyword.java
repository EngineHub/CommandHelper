package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.List;

/**
 *
 */
@Keyword.keyword("default")
public class DefaultKeyword extends Keyword {

    @Override
    public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
	return keywordPosition;
    }

    @Override
    public String docs() {
	return "Works similar to a case definition, but defines the default case, which runs if no defined cases"
		+ " match.";
    }

    @Override
    public Version since() {
	return CHVersion.V3_3_1;
    }

}
