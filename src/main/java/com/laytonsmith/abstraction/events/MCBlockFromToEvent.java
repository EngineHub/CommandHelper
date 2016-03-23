package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.core.events.BindableEvent;

/**
 * Created by bexco on 2016-03-22.
 */
public interface MCBlockFromToEvent extends BindableEvent {

    public MCBlock getBlock();

    public MCBlock getToBlock();

    public MCBlockFace getBlockFace();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);
}
