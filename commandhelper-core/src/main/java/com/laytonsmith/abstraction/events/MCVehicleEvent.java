package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.core.events.BindableEvent;

public interface MCVehicleEvent extends BindableEvent {
	public MCEntity getVehicle();
}
