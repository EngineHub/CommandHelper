package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCSlime;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCSlimeSplitEvent extends BindableEvent {

    public CInt getCount();

    public MCSlime getEntity();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

    public void setCount(int count);
}
