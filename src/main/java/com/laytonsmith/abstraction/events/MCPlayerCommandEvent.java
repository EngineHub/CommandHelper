package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author layton
 */
public interface MCPlayerCommandEvent extends BindableEvent {
    public void cancel();
    public String getCommand();

    public MCPlayer getPlayer();

    public boolean isCancelled();

    public void setCommand(String val);
}
