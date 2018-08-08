package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;

public interface MCVehicleEntityCollideEvent extends MCVehicleCollideEvent {

	MCEntity getEntity();

	boolean isCollisionCancelled();

	boolean isPickupCancelled();

	void setCollisionCancelled(boolean cancel);

	void setPickupCancelled(boolean cancel);
}
