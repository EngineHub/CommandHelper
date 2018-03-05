package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCPotionType;

public interface MCPotionData extends AbstractionObject {

	MCPotionType getType();

	boolean isExtended();

	boolean isUpgraded();
}
