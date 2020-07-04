package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;

public interface MCPlayerRespawnEvent extends MCPlayerEvent {

	void setRespawnLocation(MCLocation location);

	MCLocation getRespawnLocation();

	Boolean isBedSpawn();

	boolean isAnchorSpawn();
}
