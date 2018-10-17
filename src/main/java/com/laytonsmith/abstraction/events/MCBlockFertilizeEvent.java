package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.core.events.BindableEvent;

import java.util.List;

public interface MCBlockFertilizeEvent extends BindableEvent {

    public List<MCBlockState> getBlocks();

    public MCPlayer getPlayer();

    public MCBlock getBlock();

    public boolean isCancelled();

    public void setCancelled(boolean cancel);


}
