package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.events.BindableEvent;

public interface MCExplosionPrimeEvent extends BindableEvent {

    public boolean getFire();

    public CDouble getRadius();

    public MCEntity getEntity();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

    public void setFire(boolean fire);

    public void setRadius(float radius);

}
