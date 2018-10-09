package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBrewingStandFuelEvent extends BindableEvent {

    public MCItemStack getFuel();

    public CInt getFuelPower();

    public MCBlock getBlock();

    public boolean isConsuming();

    public boolean isCancelled();

    public void setFuelPower(int fuelPower);

    public void setConsuming(boolean consuming);

    public void setCancelled(boolean cancel);


}
