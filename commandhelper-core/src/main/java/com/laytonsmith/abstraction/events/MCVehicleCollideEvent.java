package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.enums.MCCollisionType;

public interface MCVehicleCollideEvent extends MCVehicleEvent {
	public MCCollisionType getCollisionType();
}
