package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;

public interface MCVehicleEnitityCollideEvent extends MCVehicleCollideEvent {

	MCEntity getEntity();

	boolean isCollisionCancelled();

	boolean isPickupCancelled();

	void setCollisionCancelled(boolean cancel);

	void setPickupCancelled(boolean cancel);
}
