package com.laytonsmith.core.constructs;

import com.methodscript.PureUtilities.Version;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 *
 */
public class CIdentifier extends CFunction {

	private final ParseTree contained;

	public CIdentifier(String type, ParseTree c, Target t) {
		super(type, t);
		contained = c;
	}

	@Override
	public boolean isDynamic() {
		return contained.getData().isDynamic();
	}

	public ParseTree contained() {
		return contained;
	}

	@Override
	public Version since() {
		return super.since();
	}

	@Override
	public String docs() {
		return super.docs();
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{};
	}

}
