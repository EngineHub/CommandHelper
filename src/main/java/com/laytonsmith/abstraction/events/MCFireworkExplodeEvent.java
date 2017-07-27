package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCFirework;
import com.laytonsmith.core.events.BindableEvent;

public interface MCFireworkExplodeEvent extends BindableEvent {

	public MCFirework getEntity();

}
