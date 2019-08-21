package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;

public interface MCPlayerLeaveBedEvent extends MCPlayerEvent {

	MCBlock getBed();
}
