package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;

/**
 *
 *
 */
public class Command extends Construct implements Cloneable {

    public Command(String name, Target t) {
	super(name, ConstructType.COMMAND, t);
    }

    @Override
    public Command clone() throws CloneNotSupportedException {
	return this;
    }

    @Override
    public boolean isDynamic() {
	return false;
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
