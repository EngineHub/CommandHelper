package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.core.CHVersion;

/**
 *
 * @author lsmith
 */
public class MOverride extends MAnnotation {

	@Override
	public String getName() {
		return "Override";
	}

	public String docs() {
		return "Methods that override a superclass method, or implement an interface method must be tagged with the @{Override} annotation."
				+ " It is a compile error if they are not.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}
	
}
