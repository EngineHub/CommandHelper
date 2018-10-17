package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.entity.Entity;

public interface MCEntityShootBowEvent extends BindableEvent {

	MCItemStack getBow();

	MCEntity getEntity();

	CDouble getForce();

	MCEntity getProjectile();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setProjectile(Entity projectile);

}
