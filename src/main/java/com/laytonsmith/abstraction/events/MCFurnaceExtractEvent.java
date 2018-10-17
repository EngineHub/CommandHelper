package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;

public interface MCFurnaceExtractEvent extends BindableEvent {

    public CInt getExpToDrop();

    public CInt getItemAmount();

    public MCMaterial getItemType();

    public MCPlayer getPlayer();

    public MCBlock getBlock();

    public void setExpToDrop(int exp);

}
