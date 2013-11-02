package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCVector;

/**
 *
 * @author MariuszT
 */
public interface MCBlockDispenseEvent extends MCBlockEvent {

	public MCItemStack getItem();

	public void setItem(MCItemStack item);

	public MCVector getVelocity();

	public void setVelocity(MCVector vel);

	public boolean isCancelled();

	public void setCancelled(boolean cancel);
}