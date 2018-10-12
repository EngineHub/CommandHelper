package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCSheep;
import com.laytonsmith.core.events.BindableEvent;

public interface MCSheepRegrowWoolEvent extends BindableEvent {

    public MCSheep getEnity();

    public boolean isCancelled();

    public void setCancelled(boolean cancelled);

}
