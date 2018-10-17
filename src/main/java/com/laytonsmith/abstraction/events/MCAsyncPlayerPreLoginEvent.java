package com.laytonsmith.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;

public interface MCAsyncPlayerPreLoginEvent extends BindableEvent {

    public String getAddress();

    public String getKickMessage();

    public String getLoginResult();

    public String getName();

    public String getUUID();

    public void setKickMessage(String msg);

    public void setResult(String result);

}
