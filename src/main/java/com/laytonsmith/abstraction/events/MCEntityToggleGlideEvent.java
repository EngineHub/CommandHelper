package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityToggleGlideEvent extends BindableEvent {
	boolean isGliding();
	MCEntity getEntity();
	MCEntityType getEntityType();
}
