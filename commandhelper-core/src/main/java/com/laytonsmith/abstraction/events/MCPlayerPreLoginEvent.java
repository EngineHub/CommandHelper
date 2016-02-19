package com.laytonsmith.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCPlayerPreLoginEvent extends BindableEvent{
    public String getName();
    public String getKickMessage();
    public void setKickMessage(String msg);
    public String getResult();
    public void setResult(String rst);
    public String getIP();
}
