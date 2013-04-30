package com.laytonsmith.abstraction.events;

import java.util.List;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityExplodeEvent extends BindableEvent {

	public MCEntity getEntity();
	
	public List<MCBlock> getBlocks();
	
	public void setBlocks(List<MCBlock> blocks);
	
	public MCLocation getLocation();
	
	public float getYield();
	
	public void setYield(float power);
}
