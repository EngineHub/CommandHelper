package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.enums.MCIgniteCause;

/**
 *
 * @author MariuszT
 */
public interface MCBlockIgniteEvent extends MCBlockEvent {

	public MCIgniteCause getCause();

	public MCPlayer getPlayer();

	public MCEntity getIgnitingEntity();

	public MCBlock getIgnitingBlock();
}
