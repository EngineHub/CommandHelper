package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;

public interface MCPlayerMoveEvent extends MCPlayerEvent {
	int getThreshold();
	MCLocation getFrom();
	MCLocation getTo();
	void setCancelled(boolean state);
	boolean isCancelled();
}
