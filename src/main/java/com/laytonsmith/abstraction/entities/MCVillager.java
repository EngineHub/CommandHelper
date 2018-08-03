package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.enums.MCProfession;

public interface MCVillager extends MCAgeable, MCInventoryHolder {
	MCProfession getProfession();
	void setProfession(MCProfession profession);
}
