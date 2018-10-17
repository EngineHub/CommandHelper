package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;

public interface MCCauldronLevelChangeEvent extends BindableEvent {

	MCEntity getEntity();

	CInt getNewLevel();

	CInt getOldLevel();

	CString getReason();

	MCBlock getBlock();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setNewLevel(int newLevel);

}
