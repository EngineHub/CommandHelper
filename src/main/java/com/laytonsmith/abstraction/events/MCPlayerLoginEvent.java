package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCPlayerLoginEvent extends BindableEvent{
    public String getIP();
    public String getKickMessage();
    public String getName();
    public MCPlayer getPlayer();
    public String getResult();
    public void setKickMessage(String msg);
    public void setResult(String rst);
}
