package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCProfession;

public interface MCVillager extends MCTrader {

	MCProfession getProfession();
	void setProfession(MCProfession profession);
}
