package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.util.Vector;

public interface MCPlayerVelocityEvent extends BindableEvent {

	Vector getVelocity();

	MCPlayer getPlayer();

	boolean isCancelled();

	void setCancelled(boolean cancel);

	void setVelocity(Vector velocity);

}
