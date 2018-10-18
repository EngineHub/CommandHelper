package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCAbstractHorse;
import com.laytonsmith.core.events.BindableEvent;

public interface MCHorseJumpEvent extends BindableEvent {

	MCAbstractHorse getEntity();

	float getPower();

	boolean isCancelled();

	/**
	 * @deprecated Magic value
	 */
	@Deprecated
	void setCancelled(boolean cancelled);

	/**
	 * @deprecated Magic value
	 */
	@Deprecated
	void setPower(float power);

}
