package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;

/**
 *
 */
@typeof("primitive")
public abstract class CPrimitive extends Construct {

	public CPrimitive(String value, ConstructType type, Target t) {
		super(value, type, t);
	}

	@Override
	public String docs() {
		return "A primitive is any non-object and non-array data type. All primitives are pass by value.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_0_1;
	}

}
