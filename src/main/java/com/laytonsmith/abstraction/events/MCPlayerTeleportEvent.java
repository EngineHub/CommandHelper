package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.enums.MCTeleportCause;

public interface MCPlayerTeleportEvent extends MCPlayerEvent {

	MCTeleportCause getCause();

	void setFrom(MCLocation oldloc);

	void setTo(MCLocation newloc);

	MCLocation getFrom();

	MCLocation getTo();

	void setCancelled(boolean state);

	boolean isCancelled();
}
