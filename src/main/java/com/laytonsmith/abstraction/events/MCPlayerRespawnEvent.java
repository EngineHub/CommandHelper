package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author layton
 */
public interface MCPlayerRespawnEvent extends BindableEvent {

    public MCPlayer getPlayer();

    public void setRespawnLocation(MCLocation location);

    public MCLocation getRespawnLocation();

	public Boolean isBedSpawn();

}
