package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.core.events.BindableEvent;

public interface MCExplosionPrimeEvent extends BindableEvent {

	boolean getFire();

	float getRadius();

	MCEntity getEntity();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setFire(boolean fire);

	void setRadius(float radius);

}
