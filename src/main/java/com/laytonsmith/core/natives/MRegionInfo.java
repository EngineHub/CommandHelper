package com.laytonsmith.core.natives;

import com.laytonsmith.annotations.documentation;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.natives.interfaces.MObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@typename("RegionInfo")
public class MRegionInfo extends MObject {
	
	@documentation(docs="An array of points that define this region")
	public List<MLocation> regionBounds;
	@documentation(docs="An array of owners of this region")
	public List<String> owners = new ArrayList<String>();
	@documentation(docs="An array of members of this region")
	public List<String> members = new ArrayList<String>();
	@documentation(docs="An array of arrays of this region's flags")
	public Map<String, String> flags = new HashMap<String, String>();
	@documentation(docs="This region's priority")
	public int priority;
	@documentation(docs="The volume of this region (in meters cubed)")
	public double volume;

	@Override
	protected String alias(String field) {
		if("0".equals(field)){
			return "regionBounds";
		} else if("1".equals(field)){
			return "owners";
		} else if("2".equals(field)){
			return "members";
		} else if("3".equals(field)){
			return "flags";
		} else if("4".equals(field)){
			return "priority";
		} else if("5".equals(field)){
			return "volume";
		} else {
			return null;
		}
	}
	
}
