package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.enums.MCTeleportCause;

/**
 *
 * 
 */
public interface MCPlayerTeleportEvent extends MCPlayerEvent {
	public MCTeleportCause getCause();
    public void setFrom(MCLocation oldloc);
    public void setTo(MCLocation newloc);
	public MCLocation getFrom();
	public MCLocation getTo();
	public void setCancelled(boolean state);
	public boolean isCancelled();
}