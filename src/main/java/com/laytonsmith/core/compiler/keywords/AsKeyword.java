package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.BothAssociativeLateBindingKeyword;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.DataHandling;

/**
 *
 */
@Keyword.keyword("as")
public class AsKeyword extends BothAssociativeLateBindingKeyword {

	@Override
	public String docs() {
		return "Used to cast (or crosscast) a value to a specific type.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

	@Override
	protected ParseTree process(Environment env, Target t, FileOptions fileOptions, ParseTree leftHandNode,
			ParseTree rightHandNode) throws ConfigCompileException {
		if(!(rightHandNode.getData() instanceof CClassType) && !(rightHandNode.getData() instanceof LeftHandSideType)) {
			throw new ConfigCompileException("Expected class type here.", rightHandNode.getTarget());
		}
		ParseTree cast = new ParseTree(new CFunction(DataHandling.cast.NAME, t), fileOptions, true);
		GenericParameters.GenericParametersBuilder builder
				= GenericParameters.emptyBuilder(new DataHandling.cast().getSignatures().getSignatures().get(0));
		if(rightHandNode.getData() instanceof CClassType c) {
			builder.addParameter(c, null, env, t);
		} else if(rightHandNode.getData() instanceof LeftHandSideType l) {
			builder.addParameter(l);
		}
		cast.getNodeModifiers().setGenerics(builder.build(t, env));
		cast.addChild(leftHandNode);
		return cast;
	}

}
