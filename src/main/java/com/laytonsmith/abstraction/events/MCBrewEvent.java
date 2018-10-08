package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBrewEvent extends BindableEvent {

    public MCInventory getContents();

    public CInt getFuelLevel();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

}
