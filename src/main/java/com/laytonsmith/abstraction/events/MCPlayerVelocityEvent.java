package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.util.Vector;

public interface MCPlayerVelocityEvent extends BindableEvent {

    public Vector getVelocity();

    public MCPlayer getPlayer();

    public boolean isCancelled();

    public void setCancelled(boolean cancel);

    public void setVelocity(Vector velocity);

}
