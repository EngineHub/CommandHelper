package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.enums.MCResourcePackStatus;

public interface MCPlayerResourcePackEvent extends MCPlayerEvent {
	MCResourcePackStatus getStatus();
}
