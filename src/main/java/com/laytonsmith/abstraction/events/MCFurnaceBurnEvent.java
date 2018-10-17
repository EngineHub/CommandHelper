package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCFurnaceBurnEvent extends BindableEvent {

    public CInt getBurnTine();

    public MCItemStack getFuel();

    public MCBlock getBlock();

    public boolean isBurning();

    public boolean isCancelled();

    public void setBurning(boolean burning);

    public void setBurnTime(int burnTime);

    public void setCancelled(boolean cancel);

}
