package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityAirChangeEvent extends BindableEvent {

	CInt getAmount();

	MCEntity getEntity();

	boolean isCancelled();

	void setAmount(int amount);

	void setCancelled(boolean cancelled);

}
