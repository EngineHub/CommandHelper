package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityEnterPortalEvent extends BindableEvent {
	public MCEntity getEntity();
	public MCLocation getLocation();
}
