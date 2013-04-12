package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;

/**
 *
 * @author Layton
 */
public interface MCPlayerMoveEvent extends MCPlayerEvent {
	public MCLocation getFrom();
	public MCLocation getTo();
	public void setCancelled(boolean state);
	public boolean isCancelled();
}
