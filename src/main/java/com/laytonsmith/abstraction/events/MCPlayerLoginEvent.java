package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCPlayerLoginEvent extends BindableEvent{
    public String getName();
    public String getKickMessage();
    public void setKickMessage(String msg);
    public String getResult();
    public void setResult(String rst);
    public String getIP();
    public MCPlayer getPlayer();
}
