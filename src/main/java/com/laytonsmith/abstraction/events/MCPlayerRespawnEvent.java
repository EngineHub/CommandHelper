package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;

/**
 *
 */
public interface MCPlayerRespawnEvent extends MCPlayerEvent {

    public void setRespawnLocation(MCLocation location);

    public MCLocation getRespawnLocation();

	public Boolean isBedSpawn();

}
