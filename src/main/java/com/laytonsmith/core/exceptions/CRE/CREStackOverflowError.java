package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@typeof("StackOverflowError")
public class CREStackOverflowError extends CREError {

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final CClassType TYPE = CClassType.get("StackOverflowError");

    public CREStackOverflowError(String msg, Target t) {
	super(msg, t);
    }

    public CREStackOverflowError(String msg, Target t, Throwable cause) {
	super(msg, t, cause);
    }

    @Override
    public String docs() {
	return "Thrown if a stack overflow error happens. This can occur if a"
		+ " function recurses too deeply.";
    }

    @Override
    public Version since() {
	return CHVersion.V3_3_1;
    }

    @Override
    public CClassType[] getSuperclasses() {
	return super.getSuperclasses();
    }

    @Override
    public CClassType[] getInterfaces() {
	return super.getInterfaces();
    }
}
