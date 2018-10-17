package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBlockPhysicsEvent extends BindableEvent {

    public MCMaterial getChangedType();

    public MCBlock getBlock();

    public boolean isCancelled();

    public void setCancelled(boolean cancel);

}
