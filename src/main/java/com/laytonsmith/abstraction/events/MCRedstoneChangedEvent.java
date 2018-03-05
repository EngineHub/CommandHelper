package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.core.events.BindableEvent;

public interface MCRedstoneChangedEvent extends BindableEvent {

	boolean isActive();

	MCLocation getLocation();
}
