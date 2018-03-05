package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;

public interface MCVehicleMoveEvent extends MCVehicleEvent {

	int getThreshold();

	MCLocation getFrom();

	MCLocation getTo();

	void setCancelled(boolean state);

	boolean isCancelled();
}
