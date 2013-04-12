package com.laytonsmith.abstraction.events;

/**
 *
 * @author layton
 */
public interface MCPlayerJoinEvent extends MCPlayerEvent{
    public String getJoinMessage();
    public void setJoinMessage(String message);
}
