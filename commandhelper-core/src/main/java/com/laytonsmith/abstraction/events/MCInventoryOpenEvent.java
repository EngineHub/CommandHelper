package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCHumanEntity;

/**
 *
 * @author import
 */
public interface MCInventoryOpenEvent extends MCInventoryEvent {
	public MCHumanEntity getPlayer();
}
