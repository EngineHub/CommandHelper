package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.Location;


public interface MCEntityTeleportEvent extends BindableEvent {

    public MCLocation getFrom();

    public MCLocation getTo();

    public MCEntity getEntity();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

    public void setFrom(Location from);

    public void setTo(Location to);

}
