package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.Velocity;

/**
 *
 * @author MariuszT
 */
public interface MCBlockDispenseEvent extends MCBlockEvent {

	public MCItemStack getItem();

	public void setItem(MCItemStack item);

	public Velocity getVelocity();

	public void setVelocity(Velocity vel);

	public boolean isCancelled();

	public void setCancelled(boolean cancel);
}