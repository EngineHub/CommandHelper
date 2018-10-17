package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityToggleSwimEvent extends BindableEvent {

	boolean isSwimming();

	MCEntity getEntity();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

}
