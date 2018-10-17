package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityResurrectEvent extends BindableEvent {

	MCLivingEntity getEntity();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

}
