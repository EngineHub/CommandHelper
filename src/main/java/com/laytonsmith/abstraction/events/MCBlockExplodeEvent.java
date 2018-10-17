package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.events.BindableEvent;

import java.util.List;

public interface MCBlockExplodeEvent extends BindableEvent {

	List<MCBlock> getBlockList();

	MCBlock getBlock();

	CDouble getYield();

	boolean isCancelled();

	void setYield(float yield);

	void setCancelled(boolean cancel);

}
