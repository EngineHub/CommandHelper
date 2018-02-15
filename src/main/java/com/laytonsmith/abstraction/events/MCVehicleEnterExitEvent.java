package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;

public interface MCVehicleEnterExitEvent extends MCVehicleEvent {
	MCEntity getEntity();
}
