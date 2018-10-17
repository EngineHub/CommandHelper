package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityTameEvent extends BindableEvent {

	MCLivingEntity getEntity();

	MCAnimalTamer getOwner();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

}
