package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;

/**
 *
 * @author layton
 */
public interface MCPlayerRespawnEvent extends MCPlayerEvent {

    public void setRespawnLocation(MCLocation location);

    public MCLocation getRespawnLocation();

	public Boolean isBedSpawn();

}
