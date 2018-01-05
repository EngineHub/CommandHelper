package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 *
 */
public class CPreIdentifier extends Construct {

    public CPreIdentifier(String value, Target t){
        super(value, ConstructType.IDENTIFIER, t);
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
	return new CClassType[]{Mixed.TYPE};
    }

    @Override
    public CClassType[] getInterfaces() {
	return new CClassType[]{};
    }

}
