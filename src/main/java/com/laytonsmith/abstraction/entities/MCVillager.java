package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCMerchant;
import com.laytonsmith.abstraction.enums.MCProfession;

public interface MCVillager extends MCAgeable, MCInventoryHolder {

	MCMerchant asMerchant();
	int getRiches();
	void setRiches(int riches);
	MCProfession getProfession();
	void setProfession(MCProfession profession);
}
