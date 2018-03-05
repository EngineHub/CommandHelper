package com.laytonsmith.abstraction.events;

public interface MCPlayerLoginEvent extends MCPlayerEvent {

	String getName();

	String getUniqueId();

	String getKickMessage();

	void setKickMessage(String msg);

	String getResult();

	void setResult(String rst);

	String getIP();

	String getHostname();
}
