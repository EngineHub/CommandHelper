package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.core.events.BindableEvent;

public interface MCMoistureChangeEvent extends BindableEvent {

    public MCBlockState getNewState();

    public MCBlock getBlock();

    public boolean isCancelled();

    public void setCancelled(boolean cancel);

}
