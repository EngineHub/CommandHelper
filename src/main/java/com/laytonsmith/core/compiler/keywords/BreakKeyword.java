package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.RightAssociativeLateBindingKeyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.ControlFlow;

/**
 *
 */
@Keyword.keyword("break")
public class BreakKeyword extends RightAssociativeLateBindingKeyword {

	@Override
	protected ParseTree process(Target t, FileOptions fileOptions, ParseTree rightHandNode) throws ConfigCompileException {
		ParseTree ret = new ParseTree(new CFunction(ControlFlow._break.NAME, t), fileOptions);
		if(rightHandNode != null) {
			ret.addChild(rightHandNode);
		}
		return ret;
	}

	@Override
	public boolean allowEmptyValue() {
		return true;
	}

	@Override
	public String docs() {
		return "Breaks out of a loop or switch statement. Keyword version of the break function.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_5;
	}

}
