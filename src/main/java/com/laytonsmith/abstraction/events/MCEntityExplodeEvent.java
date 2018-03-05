package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;
import java.util.List;

public interface MCEntityExplodeEvent extends BindableEvent {

	MCEntity getEntity();

	List<MCBlock> getBlocks();

	void setBlocks(List<MCBlock> blocks);

	MCLocation getLocation();

	float getYield();

	void setYield(float power);
}
