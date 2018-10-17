package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.entities.MCItem;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityDropItemEvent extends BindableEvent {

	MCItem getItemDrop();

	MCEntity getEntity();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

}
