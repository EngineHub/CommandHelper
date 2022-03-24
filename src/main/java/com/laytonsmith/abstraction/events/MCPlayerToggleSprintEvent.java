package com.laytonsmith.abstraction.events;

public interface MCPlayerToggleSprintEvent extends MCPlayerEvent {

	boolean isSprinting();

	void setCancelled(boolean state);

	boolean isCancelled();
}
