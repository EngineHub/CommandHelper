package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.entities.MCFish;
import com.laytonsmith.abstraction.enums.MCFishingState;

public interface MCPlayerFishEvent extends MCPlayerEvent {
	public MCEntity getCaught();
	public int getExpToDrop();
	public MCFish getHook();
	public MCFishingState getState();
	public void setExpToDrop(int exp);
}
