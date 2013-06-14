package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.enums.MCAction;

/**
 *
 */
public interface MCPlayerInteractEvent extends MCPlayerEvent{

    public MCAction getAction();

    public MCBlock getClickedBlock();

    public MCBlockFace getBlockFace();
    
    public MCItemStack getItem();
    
}
