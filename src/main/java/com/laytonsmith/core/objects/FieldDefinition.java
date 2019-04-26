package com.laytonsmith.core.objects;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.UnqualifiedClassName;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import java.util.Set;

/**
 * A Field is a representation of a concrete field within an object definition.
 */
@typeof("ms.lang.FieldDefinition")
public class FieldDefinition extends ElementDefinition {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(FieldDefinition.class);

	public FieldDefinition(
			AccessModifier accessModifier,
			Set<ElementModifier> elementModifiers,
			UnqualifiedClassName type,
			String name,
			ParseTree code,
			String signature,
			Target t
	) {
		super(
			accessModifier,
			elementModifiers,
			type,
			name,
			code,
			signature,
			ConstructType.FIELD,
			t
		);
	}

	@Override
	public Element createConcreteType(CClassType definedIn) {
		return new Field(this, definedIn);
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}

	@Override
	public String docs() {
		return "Creates a FieldDefinition. This is only useful as an intermediary step, and should never be"
				+ " used directly.";
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{ElementDefinition.TYPE};
	}

}
