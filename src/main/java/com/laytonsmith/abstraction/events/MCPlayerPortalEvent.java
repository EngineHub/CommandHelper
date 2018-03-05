package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCTravelAgent;

public interface MCPlayerPortalEvent extends MCPlayerTeleportEvent {

	void useTravelAgent(boolean useTravelAgent);

	boolean useTravelAgent();

	MCTravelAgent getPortalTravelAgent();

	void setPortalTravelAgent(MCTravelAgent travelAgent);
}
