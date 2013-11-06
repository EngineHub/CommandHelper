package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCVector;

/**
 *
 * @author Hekta
 */
public interface MCPlayerVelocityEvent extends MCPlayerEvent {

	public MCVector getVelocity();
	public void setVelocity(MCVector velocity);
}