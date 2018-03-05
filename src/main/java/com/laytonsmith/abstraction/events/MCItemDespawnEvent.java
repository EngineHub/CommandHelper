package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.core.events.BindableEvent;

public interface MCItemDespawnEvent extends BindableEvent {

	MCItem getEntity();

	MCLocation getLocation();
}
