package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Set;

public interface MCServerPingEvent extends BindableEvent {

	InetAddress getAddress();

	int getMaxPlayers();

	String getMOTD();

	int getNumPlayers();

	void setMaxPlayers(int max);

	void setMOTD(String motd);

	Set<MCPlayer> getPlayers();

	void setPlayers(Collection<MCPlayer> players);
}
