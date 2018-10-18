package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityShootBowEvent extends BindableEvent {

	MCItemStack getBow();

	MCEntity getEntity();

	float getForce();

	MCEntity getProjectile();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setProjectile(MCEntity projectile);

}
