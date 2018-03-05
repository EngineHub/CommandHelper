package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;

public interface MCPlayerToggleFlightEvent {

	boolean isFlying();

	MCPlayer getPlayer();

	void setCancelled(boolean state);

	boolean isCancelled();
}
