package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityToggleSwimEvent extends BindableEvent {

    public boolean isSwimming();

    public MCEntity getEntity();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

}
