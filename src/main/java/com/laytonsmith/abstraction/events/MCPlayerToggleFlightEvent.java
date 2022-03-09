package com.laytonsmith.abstraction.events;

public interface MCPlayerToggleFlightEvent extends MCPlayerEvent {

	boolean isFlying();

	void setCancelled(boolean state);

	boolean isCancelled();
}
