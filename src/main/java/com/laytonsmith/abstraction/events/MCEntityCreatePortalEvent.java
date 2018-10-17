package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;

import java.util.List;

public interface MCEntityCreatePortalEvent extends BindableEvent {

    public List<MCBlockState> getBlocks();

    public MCEntity getEntity();

    public CString getPortalType();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);
}
