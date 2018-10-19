package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;

public interface MCPrepareAnvilEvent extends MCInventoryEvent {

	MCItemStack getResult();

	int getCost();

	String getRenameText();

	void setResult(MCItemStack result);

	void setCost(int cost);

}
