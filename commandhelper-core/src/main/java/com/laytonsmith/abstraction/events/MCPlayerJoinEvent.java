package com.laytonsmith.abstraction.events;

/**
 *
 * 
 */
public interface MCPlayerJoinEvent extends MCPlayerEvent{
    public String getJoinMessage();
    public void setJoinMessage(String message);
}
