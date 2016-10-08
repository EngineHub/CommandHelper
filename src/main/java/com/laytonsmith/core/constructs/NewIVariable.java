package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;

/**
 *
 *
 */
public class NewIVariable extends Construct {

    private String name;

    public NewIVariable(String name, Target t) {
	super("", ConstructType.IVARIABLE, t);
	this.name = name;
    }

    @Override
    public boolean isDynamic() {
	return true;
    }

    public String getVariableName() {
	return name;
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
	return new CClassType[]{CClassType.MIXED};
    }

    @Override
    public CClassType[] getInterfaces() {
	return new CClassType[]{};
    }

}
