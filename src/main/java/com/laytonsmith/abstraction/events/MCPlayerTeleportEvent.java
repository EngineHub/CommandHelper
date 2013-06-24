package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.enums.MCTeleportCause;

/**
 *
 * @author layton
 */
public interface MCPlayerTeleportEvent extends MCPlayerMoveEvent {
	public MCTeleportCause getCause();
    public void setFrom(MCLocation oldloc);
    public void setTo(MCLocation newloc);
}