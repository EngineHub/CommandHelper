package com.laytonsmith.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;

public interface MCPlayerPreLoginEvent extends BindableEvent {

	String getName();

	String getKickMessage();

	void setKickMessage(String msg);

	String getResult();

	void setResult(String rst);

	String getIP();
}
