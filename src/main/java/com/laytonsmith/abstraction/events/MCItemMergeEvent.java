package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCItem;
import com.laytonsmith.core.events.BindableEvent;

public interface MCItemMergeEvent extends BindableEvent {

	MCItem getEntity();

	MCItem getTarget();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

}
