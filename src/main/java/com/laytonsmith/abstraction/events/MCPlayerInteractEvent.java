package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCAction;
import com.laytonsmith.abstraction.MCBlockFace;
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

    public MCPlayer getPlayer();

    public MCBlock getClickedBlock();

    public MCBlockFace getBlockFace();
    
    public MCItemStack getItem();
    
}
