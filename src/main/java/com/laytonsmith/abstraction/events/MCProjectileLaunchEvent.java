package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.core.events.BindableEvent;

public interface MCProjectileLaunchEvent extends BindableEvent {
	MCProjectile getEntity();
	MCEntityType getEntityType();
}
