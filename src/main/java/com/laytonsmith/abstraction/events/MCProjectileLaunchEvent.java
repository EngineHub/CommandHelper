package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.core.events.BindableEvent;

/**
 * 
 * @author Hekta
 */
public interface MCProjectileLaunchEvent extends BindableEvent {

	public MCProjectile getEntity();

	public MCEntityType getEntityType();
}
