package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;

/**
 * 
 */
@TypeofRunnerFor(Sizable.class)
public class SizableTypeofRunner implements TypeofRunnerIface {
	@Override
	public String docs() {
		return "Any object that can report a size should implement this.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}

}
