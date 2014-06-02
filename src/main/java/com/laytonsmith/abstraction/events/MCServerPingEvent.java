package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Set;

/**
 * 
 * @author jb_aero
 */
public interface MCServerPingEvent extends BindableEvent {

	public InetAddress getAddress();

	public int getMaxPlayers();

	public String getMOTD();

	public int getNumPlayers();

	public void setMaxPlayers(int max);

	public void setMOTD(String motd);

	public Set<MCPlayer> getPlayers();

	public void setPlayers(Collection<MCPlayer> players);
}