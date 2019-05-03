package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCOcelotType;

public interface MCOcelot extends MCAgeable {
	// legacy 1.13 methods
	MCOcelotType getCatType();
	boolean isSitting();
	void setCatType(MCOcelotType type);
	void setSitting(boolean sitting);
}
