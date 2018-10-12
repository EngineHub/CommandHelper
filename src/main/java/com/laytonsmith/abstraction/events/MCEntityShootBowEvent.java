package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.entity.Entity;

public interface MCEntityShootBowEvent extends BindableEvent {

    public MCItemStack getBow();

    public MCEntity getEntity();

    public CDouble getForce();

    public MCEntity getProjectile();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

    public void setProjectile(Entity projectile);

}
