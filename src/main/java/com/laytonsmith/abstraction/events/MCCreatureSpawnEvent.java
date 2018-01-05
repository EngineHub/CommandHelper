package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCSpawnReason;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author jb_aero
 */
public interface MCCreatureSpawnEvent extends BindableEvent {
	MCLivingEntity getEntity();
	MCLocation getLocation();
	MCSpawnReason getSpawnReason();
	void setType(MCEntityType type);
}
