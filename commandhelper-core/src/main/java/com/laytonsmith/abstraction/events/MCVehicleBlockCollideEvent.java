package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;

public interface MCVehicleBlockCollideEvent extends MCVehicleCollideEvent {
	public MCBlock getBlock();
}
