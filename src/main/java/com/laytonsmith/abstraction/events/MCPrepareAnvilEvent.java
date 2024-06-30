package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCItemStack;

public interface MCPrepareAnvilEvent extends MCInventoryEvent {
	MCPlayer getPlayer();

	void setResult(MCItemStack i);
}
