package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;

/**
 *
 */
@typename("Ovr")
public class MOvr extends MAnnotation {

	public String docs() {
		return "Methods that override a superclass method, or implement an interface method must be tagged with the @{Ovr} annotation."
				+ " It will generate a warning if they are not.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}

	@Override
	public MAnnotation[] getMetaAnnotations() {
		return new MAnnotation[]{
			new TargetRestriction(ElementType.METHOD)
		};
	}
	
}
