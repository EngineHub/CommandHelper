package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCHumanEntity;

/**
 *
 * @author import
 */
public interface MCInventoryOpenEvent extends MCInventoryEvent {
	public MCHumanEntity getPlayer();
}
