package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBlockRedstoneEvent extends BindableEvent {

	CInt getNewCurrent();

	CInt getOldCurrent();

	MCBlock getBlock();

	void setNewCurrent(int newCurrent);

}
