package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBrewingStandFuelEvent extends BindableEvent {

	MCItemStack getFuel();

	CInt getFuelPower();

	MCBlock getBlock();

	boolean isConsuming();

	boolean isCancelled();

	void setFuelPower(int fuelPower);

	void setConsuming(boolean consuming);

	void setCancelled(boolean cancel);


}
