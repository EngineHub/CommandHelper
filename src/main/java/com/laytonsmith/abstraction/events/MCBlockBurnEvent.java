package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author cgallarno
 */
public interface MCBlockBurnEvent extends BindableEvent {
	
	public MCBlock getBlock();
	
}
