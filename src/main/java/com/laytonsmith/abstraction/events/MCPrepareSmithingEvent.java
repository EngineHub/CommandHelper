package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;

public interface MCPrepareSmithingEvent extends MCInventoryEvent {
	MCPlayer getPlayer();

	void setResult(MCItemStack stack);
}
