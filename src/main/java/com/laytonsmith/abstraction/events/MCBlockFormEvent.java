package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlockState;

public interface MCBlockFormEvent extends MCBlockEvent {

	MCBlockState getNewState();
}
