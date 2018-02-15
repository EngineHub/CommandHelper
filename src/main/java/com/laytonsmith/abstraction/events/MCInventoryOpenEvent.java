package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCHumanEntity;

public interface MCInventoryOpenEvent extends MCInventoryEvent {
	MCHumanEntity getPlayer();
}
