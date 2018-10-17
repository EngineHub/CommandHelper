package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityResurrectEvent extends BindableEvent {

    public MCLivingEntity getEntity();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

}
