package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.entities.MCFishHook;
import com.laytonsmith.abstraction.enums.MCFishingState;
import com.laytonsmith.core.events.BindableEvent;

public interface MCPlayerFishEvent extends BindableEvent {
	public MCEntity getCaught();
	public int getExpToDrop();
	public MCFishHook getHook();
	public MCFishingState getState();
	public void setExpToDrop(int exp);
	public MCPlayer getPlayer();
}
