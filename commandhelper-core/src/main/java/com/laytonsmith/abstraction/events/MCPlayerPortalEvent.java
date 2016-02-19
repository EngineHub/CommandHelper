package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCTravelAgent;

public interface MCPlayerPortalEvent extends MCPlayerTeleportEvent {
	public void useTravelAgent(boolean useTravelAgent);
	public boolean useTravelAgent();
	public MCTravelAgent getPortalTravelAgent();
	public void setPortalTravelAgent(MCTravelAgent travelAgent);
}
