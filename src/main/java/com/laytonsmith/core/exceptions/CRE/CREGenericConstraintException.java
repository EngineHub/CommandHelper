package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;

/**
 *
 */
@typeof("ms.lang.CastException")
public class CREGenericConstraintException extends CREException {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CREGenericConstraintException.class);

	public CREGenericConstraintException(String msg, Target t) {
		super(msg, t);
	}

	public CREGenericConstraintException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if a generic definition has an invalid constraint combination. For instance,"
				+ " in the generic definition <T extends number & T super mixed>, the class would need to be both"
				+ " a superclass of mixed and a subclass of number, a state which is impossible to be in.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_5;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return super.getSuperclasses();
	}

	@Override
	public CClassType[] getInterfaces() {
		return super.getInterfaces();
	}

	@Override
	public GenericParameters getGenericParameters() {
		return null;
	}
}

