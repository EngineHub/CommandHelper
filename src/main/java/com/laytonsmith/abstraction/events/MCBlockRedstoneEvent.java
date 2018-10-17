package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBlockRedstoneEvent extends BindableEvent {

    public CInt getNewCurrent();

    public CInt getOldCurrent();

    public MCBlock getBlock();

    public void setNewCurrent(int newCurrent);

}
