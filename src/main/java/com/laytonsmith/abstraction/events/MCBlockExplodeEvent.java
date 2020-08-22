package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;

import java.util.List;

public interface MCBlockExplodeEvent extends MCBlockEvent {

	List<MCBlock> getBlocks();

	void setBlocks(List<MCBlock> blocks);

	float getYield();

	void setYield(float power);

}
