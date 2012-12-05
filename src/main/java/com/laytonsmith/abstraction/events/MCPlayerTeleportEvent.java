package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author layton
 */
public interface MCPlayerTeleportEvent extends BindableEvent{
    public MCPlayer getPlayer();
    public MCLocation getFrom();
	public MCLocation getTo();
	public String getCause();
    public void setTo(MCLocation newloc);
	public void setCancelled(boolean state);
	public boolean isCancelled();
}