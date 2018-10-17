package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCSheep;
import com.laytonsmith.core.events.BindableEvent;

public interface MCSheepRegrowWoolEvent extends BindableEvent {

	MCSheep getEnity();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

}
