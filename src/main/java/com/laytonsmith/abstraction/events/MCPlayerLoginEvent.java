package com.laytonsmith.abstraction.events;

/**
 *
 * @author EntityReborn
 */
public interface MCPlayerLoginEvent extends MCPlayerEvent{
    public String getName();
    public String getUniqueId();
    public String getKickMessage();
    public void setKickMessage(String msg);
    public String getResult();
    public void setResult(String rst);
    public String getIP();
    public String getHostname();
}
