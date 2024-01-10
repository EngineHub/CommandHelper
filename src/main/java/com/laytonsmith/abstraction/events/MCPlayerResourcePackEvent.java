package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.enums.MCResourcePackStatus;

import java.util.UUID;

public interface MCPlayerResourcePackEvent extends MCPlayerEvent {
	MCResourcePackStatus getStatus();
	UUID getId();
}
