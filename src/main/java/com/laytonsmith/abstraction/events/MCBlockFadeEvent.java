package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlockState;

public interface MCBlockFadeEvent extends MCBlockEvent {

	MCBlockState getNewState();
}
