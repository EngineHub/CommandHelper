package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityTameEvent extends BindableEvent {

    public MCLivingEntity getEntity();

    public MCAnimalTamer getOwner();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

}
