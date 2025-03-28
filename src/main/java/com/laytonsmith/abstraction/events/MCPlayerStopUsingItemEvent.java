package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;

public interface MCPlayerStopUsingItemEvent extends MCPlayerEvent {
	MCItemStack getItem();
	int getTicksHeldFor();
}
