package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.ObjectType;

/**
 *
 *
 */
public class CBrace extends Construct {

    ParseTree code;

    public CBrace(ParseTree code) {
	super(code.toString(), ConstructType.BRACE, code.getTarget());
	this.code = code;
    }

    @Override
    public boolean isDynamic() {
	return true;
    }

    public ParseTree getNode() {
	return code;
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
