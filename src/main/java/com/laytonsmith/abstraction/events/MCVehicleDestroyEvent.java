package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;

public interface MCVehicleDestroyEvent extends MCVehicleEvent{
    public MCEntity getAttacker();
}
