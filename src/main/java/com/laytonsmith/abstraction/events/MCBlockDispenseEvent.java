package com.laytonsmith.abstraction.events;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCItemStack;

/**
 *
 * @author MariuszT
 */
public interface MCBlockDispenseEvent extends MCBlockEvent {

	public MCItemStack getItem();

	public void setItem(MCItemStack item);

	public Vector3D getVelocity();

	public void setVelocity(Vector3D vel);

	public boolean isCancelled();

	public void setCancelled(boolean cancel);
}