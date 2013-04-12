package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public interface MCPlayerBedEvent extends MCPlayerEvent {
	public MCBlock getBed();
}
