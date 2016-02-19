package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;
import java.util.List;

public interface MCEntityExplodeEvent extends BindableEvent {

	public MCEntity getEntity();
	
	public List<MCBlock> getBlocks();
	
	public void setBlocks(List<MCBlock> blocks);
	
	public MCLocation getLocation();
	
	public float getYield();
	
	public void setYield(float power);
}
