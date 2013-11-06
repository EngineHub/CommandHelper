package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCEntity;

public interface MCVehicleEnitityCollideEvent extends MCVehicleCollideEvent {
	public MCEntity getEntity();
	public boolean isCollisionCancelled();
	public boolean isPickupCancelled();
	public void setCollisionCancelled(boolean cancel);
	public void setPickupCancelled(boolean cancel);
}
