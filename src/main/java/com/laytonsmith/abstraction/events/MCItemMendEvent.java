package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.entities.MCExperienceOrb;
import com.laytonsmith.core.events.BindableEvent;

public interface MCItemMendEvent extends BindableEvent {

	MCExperienceOrb getExperienceOrb();

	MCItemStack getItem();

	int getRepairAmount();

	MCPlayer getPlayer();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setRepairAmount(int amount);

}
