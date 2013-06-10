package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.compiler.CompilerAwareAnnotation;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * 
 */
@typename("FormatString")
public class FormatString extends MAnnotation implements CompilerAwareAnnotation {

	public String value = ".*";
	
	public FormatString(String regex){
		this.value = regex;
	}
	
	public String docs() {
		return "This annotation is used to tag an argument that is meant to be formatted in a"
				+ " specific way. The value is interpreted as a regular expression. If the value"
				+ " is null, the validation is skipped.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}

	@Override
	public MAnnotation[] getMetaAnnotations() {
		return new MAnnotation[]{
			new TargetRestriction(ElementType.ASSIGNABLE),
			new TypeRestriction(CString.class)
		};
	}

	public void validateParameter(Mixed parameter, Target t) throws ConfigRuntimeException {
		if(parameter == null){
			return;
		}
		String s = parameter.val();
		if(!s.matches(value)){
			throw new Exceptions.FormatException("Received \"" + s + "\" but was expecting the value to match the regex: " + value, t);
		}
	}
	
}
