package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.entities.MCFishHook;
import com.laytonsmith.abstraction.enums.MCFishingState;

public interface MCPlayerFishEvent extends MCPlayerEvent {
	MCEntity getCaught();
	int getExpToDrop();
	MCFishHook getHook();
	MCFishingState getState();
	void setExpToDrop(int exp);
}
