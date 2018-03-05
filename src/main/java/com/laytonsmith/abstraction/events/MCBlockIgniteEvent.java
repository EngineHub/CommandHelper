package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.enums.MCIgniteCause;

public interface MCBlockIgniteEvent extends MCBlockEvent {

	MCIgniteCause getCause();

	MCPlayer getPlayer();

	MCEntity getIgnitingEntity();

	MCBlock getIgnitingBlock();
}
