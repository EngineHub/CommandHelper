package com.laytonsmith.abstraction.events;

/**
 *
 * @author jb_aero
 */
public interface MCPlayerKickEvent extends MCPlayerEvent{
    public String getMessage();
    public void setMessage(String message);
    public String getReason();
    public void setReason(String message);
    public boolean isCancelled();
    public void setCancelled(boolean cancelled);
}
