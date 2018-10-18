package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCSheep;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.core.events.BindableEvent;

public interface MCSheepDyeWoolEvent extends BindableEvent {

	MCDyeColor getColor();

	MCSheep getEntity();

	boolean isCancelled();

	void setCancelled(boolean cancelled);

	void setColor(MCDyeColor color);

}
