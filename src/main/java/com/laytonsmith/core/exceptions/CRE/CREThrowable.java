package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@typeof("Throwable")
public class CREThrowable extends AbstractCREException {

    @ForceImplementation
    public CREThrowable(String msg, Target t) {
	super(msg, t);
    }

    @ForceImplementation
    public CREThrowable(String msg, Target t, Throwable cause) {
	super(msg, t, cause);
    }

    @Override
    public String docs() {
	return "All throwable types must extend this class. Otherwise, they will not be allowed to be thrown"
		+ " by the in script system.";
    }

    @Override
    public Version since() {
	return CHVersion.V3_3_1;
    }

    /**
     * Subclasses can reliably return super.getSuperclasses() for this,
     * if it follows the rule that it only has one superclass, and that
     * superclass is the underlying java object as well.
     * @return
     */
    @Override
    public CClassType[] getSuperclasses() {
	if(this.getClass() == CREThrowable.class) {
	    return new CClassType[]{CClassType.MIXED};
	} else {
	    return new CClassType[]{CClassType.build(this.getClass().getSuperclass().getAnnotation(typeof.class).value())};
	}
    }

    /**
     * Subclasses can reliably return super.getInterfaces() for this, if
     * it follows the rule that it does not implement any interfaces.
     */
    @Override
    public CClassType[] getInterfaces() {
	if(this.getClass() == CREThrowable.class) {
	    return new CClassType[] { CClassType.build("ArrayAccess") };
	} else {
	    return new CClassType[]{};
	}
    }

}
