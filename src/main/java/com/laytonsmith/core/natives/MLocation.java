package com.laytonsmith.core.natives;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.documentation;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.natives.interfaces.MObject;

/**
 *
 */
@typename("Location")
public class MLocation extends MObject implements Documentation {
	
	public MLocation(){
		
	}
	
	public MLocation(MCLocation loc){
		this.x = loc.getX();
		this.y = loc.getY();
		this.z = loc.getZ();
		this.world = loc.getWorld().getName();
		this.yaw = loc.getYaw();
		this.pitch = loc.getPitch();
	}
	
	public MCLocation getMCLocation(){
		return StaticLayer.GetLocation(StaticLayer.GetServer().getWorld(world), x, y, z, yaw, pitch);
	}
	
	@documentation(docs="The x coordinate in this location")
	public Double x;
	@documentation(docs="The y coordinate in this location")
	public Double y;
	@documentation(docs="The z coordinate in this location")
	public Double z;
	@documentation(docs="The world that this location is in")
	public String world;
	@documentation(docs="The yaw of this location (left and right), from 0 to 360")
	public Float yaw;
	@documentation(docs="The pitch of this location (up and down), from -90 to 90")
	public Float pitch;

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

	@Override
	public String docs() {
		return "A location array represents a location in the map.";
	}

	@Override
	public CHVersion since() {
		return CHVersion.V3_3_0;
	}
	
}
