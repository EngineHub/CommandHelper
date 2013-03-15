package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.compiler.CompilerAwareAnnotation;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
@typename("NonNull")
public class NonNull extends MAnnotation implements CompilerAwareAnnotation {

	public String docs() {
		return "If a field is tagged with this, it cannot be null. This is sometimes able to be caught by the compiler, but"
				+ " otherwise it is a runtime exception.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}

	public void validateParameter(Mixed parameter, Target t) throws ConfigRuntimeException {
		if(parameter.isNull()){
			throw new ConfigRuntimeException("NullPointerException: The parameter cannot be null", Exceptions.ExceptionType.NullPointerException, t);
		}
	}

	@Override
	public MAnnotation[] getMetaAnnotations() {
		return new MAnnotation[]{
			new TargetRestriction(ElementType.ASSIGNABLE)
		};
	}
	
}
