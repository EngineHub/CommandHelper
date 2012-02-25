package com.laytonsmith.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.abstraction.MCPlayer;

/**
 *
 * @author layton
 */
public interface MCPlayerJoinEvent extends BindableEvent{
    public MCPlayer getPlayer();
    public String getJoinMessage();
    public void setJoinMessage(String message);
}
