package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCItem;
import com.laytonsmith.core.events.BindableEvent;

public interface MCItemMergeEvent extends BindableEvent {

    public MCItem getEntity();

    public MCItem getTarget();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

}
