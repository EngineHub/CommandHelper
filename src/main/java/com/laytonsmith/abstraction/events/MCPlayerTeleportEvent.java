package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;

/**
 *
 * @author layton
 */
public interface MCPlayerTeleportEvent extends MCPlayerEvent{
    public MCLocation getFrom();
	public MCLocation getTo();
	public String getCause();
    public void setTo(MCLocation newloc);
	public void setCancelled(boolean state);
	public boolean isCancelled();
}