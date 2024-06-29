package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCItemStack;

import java.util.List;

public interface MCPrepareAnvilEvent extends MCInventoryEvent {
	List<MCHumanEntity> getViewers();

	void setResult(MCItemStack i);
}
