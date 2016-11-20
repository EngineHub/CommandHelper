package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCProfession;

public interface MCZombieVillager extends MCZombie {

	MCProfession getProfession();
	void setProfession(MCProfession profession);

}