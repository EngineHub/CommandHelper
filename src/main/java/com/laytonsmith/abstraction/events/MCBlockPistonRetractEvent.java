package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;

public interface MCBlockPistonRetractEvent extends MCBlockPistonEvent {

	MCLocation getRetractedLocation();
}
