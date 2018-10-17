package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCLeavesDecayEvent extends BindableEvent {

    public MCBlock getBlock();

    public boolean isCancelled();

    public void setCancelled(boolean cancel);

}
