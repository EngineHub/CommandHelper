package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.entities.MCItem;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityDropItemEvent extends BindableEvent {

    public MCItem getItemDrop();

    public MCEntity getEntity();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

}
