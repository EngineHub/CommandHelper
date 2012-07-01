package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCAction;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author layton
 */
public interface MCPlayerInteractEvent extends BindableEvent{

    public MCAction getAction();

    public MCBlockFace getBlockFace();

    public MCBlock getClickedBlock();

    public MCItemStack getItem();
    
    public MCPlayer getPlayer();
    
}
