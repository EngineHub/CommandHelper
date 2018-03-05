package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;

public interface MCPlayerInteractEntityEvent extends MCPlayerEvent {

	MCEntity getEntity();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	MCEquipmentSlot getHand();
}
