package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.enums.MCEnterBedResult;

public interface MCPlayerEnterBedEvent extends MCPlayerEvent {

	MCBlock getBed();
	MCEnterBedResult getResult();
}
