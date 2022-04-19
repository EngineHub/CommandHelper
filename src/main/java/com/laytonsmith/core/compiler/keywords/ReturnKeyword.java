package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.RightAssociativeLateBindingKeyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.functions.ControlFlow;

/**
 *
 */
@Keyword.keyword("return")
public class ReturnKeyword extends RightAssociativeLateBindingKeyword {

	@Override
	protected ParseTree process(Target t, FileOptions fileOptions, ParseTree rightHandNode) {
		ParseTree ret = new ParseTree(new CFunction(ControlFlow._return.NAME, t), fileOptions);
		if(rightHandNode != null) {
			ret.addChild(rightHandNode);
		}
		return ret;
	}

	@Override
	public String docs() {
		return "Returns the specified value from the Callable.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_5;
	}

	@Override
	public boolean allowEmptyValue() {
		return true;
	}
}
