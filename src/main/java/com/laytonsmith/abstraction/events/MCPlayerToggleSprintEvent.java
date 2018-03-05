package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;

public interface MCPlayerToggleSprintEvent {

	boolean isSprinting();

	MCPlayer getPlayer();

	void setCancelled(boolean state);

	boolean isCancelled();
}
