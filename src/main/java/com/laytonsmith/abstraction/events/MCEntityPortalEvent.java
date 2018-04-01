package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCTravelAgent;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityPortalEvent extends BindableEvent {

	MCEntity getEntity();

	void setTo(MCLocation newloc);

	MCLocation getFrom();

	MCLocation getTo();

	void setCancelled(boolean state);

	boolean isCancelled();

	void useTravelAgent(boolean useTravelAgent);

	MCTravelAgent getPortalTravelAgent();
}
