package com.laytonsmith.core.compiler;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.ObjectType;

/**
 *
 *
 */
public class ProcedureUsage extends Construct {

    ProcedureDefinition definition;

    public ProcedureUsage(String name, Target t) {
	super(name, ConstructType.FUNCTION, t);
    }

    @Override
    public boolean isDynamic() {
	return definition.isDynamic();
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

    @Override
    public ObjectType getObjectType() {
	return super.getObjectType();
    }

}
