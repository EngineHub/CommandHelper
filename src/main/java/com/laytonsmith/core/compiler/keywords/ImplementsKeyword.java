/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
@Keyword.keyword("implements")
public class ImplementsKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		throw new ConfigCompileException("Unexpected use of \"implements\" keyword",
				list.get(keywordPosition).getTarget());
	}

	@Override
	public String docs() {
		return "When used in a type definition, indicates that the class implements the given interfaces.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}

}
