package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCAgeable;
import com.laytonsmith.abstraction.enums.MCProfession;

public interface MCVillager extends MCAgeable {

	MCProfession getProfession();

	void setProfession(MCProfession profession);
}
