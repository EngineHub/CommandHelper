package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MVector3D;

/**
 *
 * @author MariuszT
 */
public interface MCBlockDispenseEvent extends MCBlockEvent {

	public MCItemStack getItem();

	public void setItem(MCItemStack item);

	public MVector3D getVelocity();

	public void setVelocity(MVector3D vel);

	public boolean isCancelled();

	public void setCancelled(boolean cancel);
}