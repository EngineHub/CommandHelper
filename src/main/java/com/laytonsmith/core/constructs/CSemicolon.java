package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;

/**
 * Represents a semicolon at the end of a statement. Forces the statement to close out.
 */
public class CSemicolon extends Construct {

	public CSemicolon(Target t) {
		super(";", ConstructType.TOKEN, t);
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	// None of this should ever be called, since it's not in the api.

	@Override
	public String docs() {
		return "";
	}

	@Override
	public CClassType[] getSuperclasses() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public Version since() {
		return MSVersion.V0_0_0;
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}



}
