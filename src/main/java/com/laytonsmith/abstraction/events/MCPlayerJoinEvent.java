package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author layton
 */
public interface MCPlayerJoinEvent extends BindableEvent{
    public String getJoinMessage();
    public MCPlayer getPlayer();
    public void setJoinMessage(String message);
}
