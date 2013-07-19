package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author MariuszT
 */
public interface MCBlockEvent extends BindableEvent {

	public MCBlock getBlock();
}