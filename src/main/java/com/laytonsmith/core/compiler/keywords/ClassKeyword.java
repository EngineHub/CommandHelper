package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.compiler.Keyword;
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
