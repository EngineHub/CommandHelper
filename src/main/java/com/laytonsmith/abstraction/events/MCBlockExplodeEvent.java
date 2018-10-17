package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.events.BindableEvent;

import java.util.List;

public interface MCBlockExplodeEvent extends BindableEvent {

    public List<MCBlock> getBlockList();

    public MCBlock getBlock();

    public CDouble getYield();

    public boolean isCancelled();

    public void setYield(float yield);

    public void setCancelled(boolean cancel);

}
