package com.laytonsmith.abstraction.events;

/**
 *
 * @author EntityReborn
 */
public interface MCPlayerQuitEvent extends MCPlayerEvent{
    public String getMessage();
    public void setMessage(String message);
}
