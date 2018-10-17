package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.Location;


public interface MCEntityTeleportEvent extends BindableEvent {

	MCLocation getFrom();

	MCLocation getTo();

	MCEntity getEntity();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setFrom(Location from);

	void setTo(Location to);

}
