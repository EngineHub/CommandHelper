package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;

public interface MCCauldronLevelChangeEvent extends BindableEvent {

    public MCEntity getEntity();

    public CInt getNewLevel();

    public CInt getOldLevel();

    public CString getReason();

    public MCBlock getBlock();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

    public void setNewLevel(int newLevel);

}
