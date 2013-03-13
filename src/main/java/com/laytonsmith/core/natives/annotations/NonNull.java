package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;

/**
 *
 * @author lsmith
 */
@typename("Ranged")
public class NonNull extends MAnnotation {

	public String docs() {
		return "If a field is tagged with this, it cannot be null. This is sometimes able to be caught by the compiler, but"
				+ " otherwise it is a runtime exception.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}
	
}
