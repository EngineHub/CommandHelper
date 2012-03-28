package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author layton
 */
public interface MCPlayerCommandEvent extends BindableEvent {
    public String getCommand();
    public MCPlayer getPlayer();

    public void cancel();

    public void setCommand(String val);

    public boolean isCancelled();
}
