package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBrewEvent extends BindableEvent {

    public MCInventory getContents();

    public CInt getFuelLevel();

    public MCBlock getBlock();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

}
