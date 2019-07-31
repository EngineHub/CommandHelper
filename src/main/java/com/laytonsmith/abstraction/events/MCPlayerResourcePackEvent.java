package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.enums.MCResourcePackStatus;

public interface MCPlayerResourcePackEvent {
	MCPlayer getPlayer();
	MCResourcePackStatus getStatus();
}
