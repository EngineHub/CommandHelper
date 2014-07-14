package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author KingFisher
 */
public interface MCWorldEvent extends BindableEvent {

	public MCWorld getWorld();
}