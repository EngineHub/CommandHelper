package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;

public interface MCPlayerItemConsumeEvent extends MCPlayerEvent {

	MCItemStack getItem();

	void setItem(MCItemStack item);

	MCEquipmentSlot getHand();

}
