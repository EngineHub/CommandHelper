package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCPotionType;

public interface MCPotionData extends AbstractionObject {

	public MCPotionType getType();
	public boolean isExtended();
	public boolean isUpgraded();

}
