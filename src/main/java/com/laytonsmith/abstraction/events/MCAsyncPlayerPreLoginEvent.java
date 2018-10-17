package com.laytonsmith.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;

public interface MCAsyncPlayerPreLoginEvent extends BindableEvent {

	String getAddress();

	String getKickMessage();

	String getLoginResult();

	String getName();

	String getUUID();

	void setKickMessage(String msg);

	void setResult(String result);

}
