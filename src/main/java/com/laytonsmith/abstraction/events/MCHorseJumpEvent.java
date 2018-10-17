package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCAbstractHorse;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.events.BindableEvent;

public interface MCHorseJumpEvent extends BindableEvent {

	MCAbstractHorse getEntity();

	CDouble getPower();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setPower(float power);

}
