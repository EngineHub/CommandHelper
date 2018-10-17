package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCLightningStrike;
import com.laytonsmith.abstraction.entities.MCPig;
import com.laytonsmith.abstraction.entities.MCPigZombie;
import com.laytonsmith.core.events.BindableEvent;

public interface MCPigZapEvent extends BindableEvent {

	MCPig getEntity();

	MCLightningStrike getLightning();

	MCPigZombie getPigZombie();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

}
