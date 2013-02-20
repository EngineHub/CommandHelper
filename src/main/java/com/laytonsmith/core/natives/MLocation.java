package com.laytonsmith.core.natives;

import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.natives.interfaces.MObject;

/**
 *
 * @author lsmith
 */
@api
@typename("Location")
public class MLocation extends MObject implements Documentation {
	
	public Double x;
	public Double y;
	public Double z;
	public String world;
	public Double yaw;
	public Double pitch;

	@Override
	protected String alias(String field) {
		if("0".equals(field)){
			return "x";
		} else if("1".equals(field)){
			return "y";
		} else if("2".equals(field)){
			return "z";
		} else if("3".equals(field)){
			return "world";
		} else if("4".equals(field)){
			return "yaw";
		} else if("5".equals(field)){
			return "pitch";
		} else {
			return null;
		}
	}

	public String getName() {
		return "Location";
	}

	public String docs() {
		return "A location array represents a location in the map.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_0;
	}
	
}
