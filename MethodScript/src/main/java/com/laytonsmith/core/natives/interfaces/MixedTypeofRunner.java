package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;

/**
 * 
 */
@TypeofRunnerFor(Mixed.class)
public class MixedTypeofRunner implements TypeofRunnerIface {
	
	@Override
	public String docs(){
		return "";
	}
	
	@Override
	public Version since(){
		return CHVersion.V3_0_1;
	}
	
}
