package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author jb_aero
 */
public interface MCPlayerKickEvent extends BindableEvent{
    public String getMessage();
    public void setMessage(String message);
    public String getReason();
    public void setReason(String message);
    public boolean isCancelled();
    public void setCancelled(boolean cancelled);
    public MCPlayer getPlayer();
}
