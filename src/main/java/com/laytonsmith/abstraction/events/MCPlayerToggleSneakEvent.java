
package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;

public interface MCPlayerToggleSneakEvent {
	boolean isSneaking();
	MCPlayer getPlayer();
	void setCancelled(boolean state);
	boolean isCancelled();
}
