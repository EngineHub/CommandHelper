package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;

/**
 *
 * 
 */
public interface MCPlayerMoveEvent extends MCPlayerEvent {
	public int getThreshold();
	public MCLocation getFrom();
	public MCLocation getTo();
	public void setCancelled(boolean state);
	public boolean isCancelled();
}
