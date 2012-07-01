package com.laytonsmith.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCPlayerPreLoginEvent extends BindableEvent{
    public String getIP();
    public String getKickMessage();
    public String getName();
    public String getResult();
    public void setKickMessage(String msg);
    public void setResult(String rst);
}
