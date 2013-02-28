package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.core.CHVersion;

/**
 * 
 */
public class FormatString extends MAnnotation {

	public String value = ".*";
	
	public FormatString(String regex){
		this.value = regex;
	}
	
	public String docs() {
		return "This annotation is used to tag an argument that is meant to be formatted in a"
				+ " specific way. The value is interpreted as a regular expression.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}
	
}
