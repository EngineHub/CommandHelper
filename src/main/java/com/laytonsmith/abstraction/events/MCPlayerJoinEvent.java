package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author layton
 */
public interface MCPlayerJoinEvent extends BindableEvent{
    public MCPlayer getPlayer();
    public String getJoinMessage();
    public void setJoinMessage(String message);
}
