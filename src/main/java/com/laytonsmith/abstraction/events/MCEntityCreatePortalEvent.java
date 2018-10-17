package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;

import java.util.List;

public interface MCEntityCreatePortalEvent extends BindableEvent {

	List<MCBlockState> getBlocks();

	MCEntity getEntity();

	CString getPortalType();

	boolean isCancelled();

	void setCancelled(boolean cancelled);
}
