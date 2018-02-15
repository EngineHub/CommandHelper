package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;

public interface MCPlayerBedEvent extends MCPlayerEvent {
	MCBlock getBed();
}
