package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import java.util.List;

/**
 *
 * @author EntityReborn
 */
public interface MCPlayerQuitEvent extends BindableEvent{
    public String getMessage();
    public void setMessage(String message);
    public MCPlayer getPlayer();
}
