package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCCauldronLevelChangeEvent extends BindableEvent {

	MCEntity getEntity();

	int getNewLevel();

	int getOldLevel();

	String getReason();

	MCBlock getBlock();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setNewLevel(int newLevel);

}
