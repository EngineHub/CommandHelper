package com.laytonsmith.abstraction.events;

/**
 *
 * 
 */
public interface MCPlayerCommandEvent extends MCPlayerEvent {
    public String getCommand();

    public void cancel();

    public void setCommand(String val);

    public boolean isCancelled();
}
