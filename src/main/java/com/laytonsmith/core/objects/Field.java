package com.laytonsmith.core.objects;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;

/**
 *
 */
@typeof("ms.lang.Field")
public class Field extends Element {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(Field.class);

	public Field(FieldDefinition definition, CClassType definedIn) {
		super(definition, definedIn);
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}

	@Override
	public String docs() {
		return "Represents a Field defined within a class.";
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Element.TYPE};
	}



}
