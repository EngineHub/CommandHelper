package com.laytonsmith.core.natives;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.natives.interfaces.MObject;

/**
 *
 * @author lsmith
 */
@api
public class MLocation extends MObject implements Documentation {
	
	public Double x;
	public Double y;
	public Double z;
	public String world;
	public Double yaw;
	public Double pitch;

	public String getName() {
		return "Location";
	}

	public String docs() {
		return "";
	}

	public CHVersion since() {
		return CHVersion.V3_3_0;
	}
	
}
