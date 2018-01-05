package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@typeof("BadEntityException")
public class CREBadEntityException extends CREException {

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final CClassType TYPE = CClassType.get("BadEntityException");

    public CREBadEntityException(String msg, Target t) {
        super(msg, t);
    }

    public CREBadEntityException(String msg, Target t, Throwable cause) {
        super(msg, t, cause);
    }

    @Override
    public String docs() {
        return "Thrown if an entity is looked up by id, but doesn't exist.";
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
