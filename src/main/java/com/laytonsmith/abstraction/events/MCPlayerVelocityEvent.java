package com.laytonsmith.abstraction.events;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

public interface MCPlayerVelocityEvent extends BindableEvent {

	Vector3D getVelocity();

	MCPlayer getPlayer();

	boolean isCancelled();

	void setCancelled(boolean cancel);

	void setVelocity(Vector3D velocity);

}
