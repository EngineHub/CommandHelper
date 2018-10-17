package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.entities.MCLightningStrike;
import com.laytonsmith.core.events.BindableEvent;

public interface MCCreeperPowerEvent extends BindableEvent  {

	String getCause();

	MCEntity getEntity();

	MCLightningStrike getLightning();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

}
