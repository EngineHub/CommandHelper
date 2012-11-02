/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author Layton
 */
public interface MCPlayerMovedEvent extends BindableEvent {
	public MCPlayer getPlayer();
	public MCLocation getFrom();
	public MCLocation getTo();
	public void setCancelled(boolean state);
	public boolean isCancelled();
}
