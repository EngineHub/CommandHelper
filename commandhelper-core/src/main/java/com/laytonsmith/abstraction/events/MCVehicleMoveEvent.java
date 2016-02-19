package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;

/**
 *
 * @author MariuszT
 */
public interface MCVehicleMoveEvent extends MCVehicleEvent {
	public int getThreshold();

	public MCLocation getFrom();

	public MCLocation getTo();

	public void setCancelled(boolean state);

	public boolean isCancelled();
}
