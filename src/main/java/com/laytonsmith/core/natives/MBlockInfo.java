package com.laytonsmith.core.natives;

import com.laytonsmith.annotations.documentation;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.natives.interfaces.MObject;

/**
 * 
 */
@typename("BlockInfo")
public class MBlockInfo extends MObject implements Documentation {
	
	@documentation(docs="If a block is solid (i.e. dirt or stone, as opposed to a torch or water)")
	public boolean solid;
	@documentation(docs="Indicates if a block can catch fire")
	public boolean flammable;
	@documentation(docs="Indicates if light can pass through")
	public boolean transparent;
	@documentation(docs="Indicates if the block full blocks vision")
	public boolean occluding;
	@documentation(docs="Indicates if the block can burn away")
	public boolean burnable;

	@Override
	public String docs() {
		return "Contains read-only information about a block";
	}

	@Override
	public CHVersion since() {
		return CHVersion.V3_3_1;
	}
	
}
