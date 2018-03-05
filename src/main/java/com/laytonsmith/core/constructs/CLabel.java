package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.constructs.Construct.ConstructType;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 *
 */
public class CLabel extends Construct {

	Construct label;

	public CLabel(Construct value) {
		super(value.val(), ConstructType.LABEL, value.getTarget());
		label = value;
	}

	public Construct cVal() {
		return label;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public String toString() {
		return label.toString() + ":";
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
