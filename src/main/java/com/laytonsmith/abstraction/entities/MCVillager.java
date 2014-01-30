package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCAgeable;
import com.laytonsmith.abstraction.enums.MCProfession;

/**
 *
 * @author Hekta
 */
public interface MCVillager extends MCAgeable {
	public MCProfession getProfession();
	public void setProfession(MCProfession profession);
}