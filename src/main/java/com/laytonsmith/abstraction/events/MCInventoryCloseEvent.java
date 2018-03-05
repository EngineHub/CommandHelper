package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCHumanEntity;

public interface MCInventoryCloseEvent extends MCInventoryEvent {

	MCHumanEntity getPlayer();
}
