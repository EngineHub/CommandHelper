package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.core.objects.ObjectType;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 */
@Keyword.keyword("class")
public class ClassKeyword extends ObjectDefinitionKeyword {

	@Override
	public int process(TokenStream stream, Environment env, int keywordPosition) throws ConfigCompileException {
		throw new ConfigCompileException("Keyword not supported yet.", stream.get(keywordPosition).getTarget());
	}


	@Override
	public String docs() {
		return "Defines a new class";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}

	@Override
	protected ObjectType getObjectType(Set<ObjectModifier> modifiers) {
		if(modifiers.contains(ObjectModifier.ABSTRACT)) {
			return ObjectType.ABSTRACT;
		} else {
			return ObjectType.CLASS;
		}
	}

	@Override
	protected Set<ObjectModifier> illegalModifiers() {
		return EnumSet.noneOf(ObjectModifier.class);
	}

}
