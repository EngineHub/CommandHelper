package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.enums.MCMobs;
import com.laytonsmith.abstraction.enums.MCSpawnReason;
import com.laytonsmith.core.events.BindableEvent;

/**
 * 
 * @author jb_aero
 */
public interface MCCreatureSpawnEvent extends BindableEvent {

	public MCLivingEntity getEntity();
	
	public MCLocation getLocation();
	
	public MCSpawnReason getSpawnReason();
	
	public void setType(MCMobs type);
}
