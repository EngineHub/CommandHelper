package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBlockRedstoneEvent extends BindableEvent {

	int getNewCurrent();

	int getOldCurrent();

	MCBlock getBlock();

	void setNewCurrent(int newCurrent);

}
