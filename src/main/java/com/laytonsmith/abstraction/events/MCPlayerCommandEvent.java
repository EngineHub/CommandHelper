package com.laytonsmith.abstraction.events;

/**
 *
 * @author layton
 */
public interface MCPlayerCommandEvent extends MCPlayerEvent {
    public String getCommand();

    public void cancel();

    public void setCommand(String val);

    public boolean isCancelled();
}
