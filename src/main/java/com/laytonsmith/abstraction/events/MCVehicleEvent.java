package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCVehicle;
import com.laytonsmith.core.events.BindableEvent;

public interface MCVehicleEvent extends BindableEvent {
	public MCVehicle getVehicle();
}
