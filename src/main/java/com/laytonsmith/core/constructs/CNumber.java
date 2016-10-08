package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;

/**
 *
 */
@typeof("number")
public abstract class CNumber extends CPrimitive {

    public CNumber(String value, ConstructType type, Target t) {
	super(value, type, t);
    }

    @Override
    public String docs() {
	throw new UnsupportedOperationException();
    }

    @Override
    public Version since() {
	throw new UnsupportedOperationException();
    }

    @Override
    public CClassType[] getSuperclasses() {
	throw new UnsupportedOperationException();
    }

    @Override
    public CClassType[] getInterfaces() {
	throw new UnsupportedOperationException();
    }

}
